import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SegmentService implements Runnable {
    private static final Map<String, String> validTokenIds = new ConcurrentHashMap<>();
    private static final Set<String> finishedVehicles = ConcurrentHashMap.newKeySet();
    private static final Set<String> expectedVehicles = ConcurrentHashMap.newKeySet();
    private static final Object raceLock = new Object();

    public static void setLatestTokenId(String vehicleId, String tokenId) {
        validTokenIds.put(vehicleId, tokenId);
    }

    public static void setExpectedVehicles(List<String> vehicleIds) {
        expectedVehicles.addAll(vehicleIds);
    }

    public static boolean isValidToken(RaceToken token) {
        return token.tokenId != null && token.tokenId.equals(validTokenIds.get(token.vehicleId));
    }

    private final String segmentId;
    private final String type;
    private final List<String> nextSegments;
    private final KafkaProducerService producer;
    private final KafkaConsumer<String, String> consumer;
    private final ObjectMapper mapper = new ObjectMapper();

    public SegmentService(String segmentId, String type, List<String> nextSegments, String brokers, KafkaProducerService producer) {
        this.segmentId = segmentId;
        this.type = type;
        this.nextSegments = nextSegments;
        this.producer = producer;

        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", "group-" + segmentId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(segmentId));
    }

    @Override
    public void run() {
        System.out.println("Segment " + segmentId + " gestartet (Typ: " + type + ")");
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                processToken(record.value());
            }
        }
    }

    private void processToken(String tokenJson) {
        try {
            RaceToken token = mapper.readValue(tokenJson, RaceToken.class);
            if (!isValidToken(token)) return;

            switch (type) {
                case "start-goal":
                    token.lapCount++;
                    System.out.println(token.vehicleId + " erreicht " + segmentId + ", Runde: " + token.lapCount);
                    if (token.lapCount >= token.maxLaps) {
                        long totalTime = System.currentTimeMillis() - token.startTime;
                        System.out.println(token.vehicleId + " hat das Rennen beendet in " + totalTime + " ms");
                        finishedVehicles.add(token.vehicleId);
                        synchronized (raceLock) {
                            if (finishedVehicles.containsAll(expectedVehicles)) {
                                System.out.println("Alle Streitwagen sind im Ziel. Rennen beendet.");
                                System.exit(0);
                            }
                        }
                        return;
                    }
                    break;
                case "caesar":
                    System.out.println(token.vehicleId + " grüßt Caesar am Segment " + segmentId);
                    break;
                case "bottleneck":
                    int delay = ThreadLocalRandom.current().nextInt(500, 1500);
                    System.out.println(token.vehicleId + " steckt im Engpass " + segmentId + ", wartet " + delay + "ms");
                    Thread.sleep(delay);
                    break;
                default:
                    System.out.println("Segment " + segmentId + " leitet " + token.vehicleId + " weiter.");
            }

            String updatedToken = mapper.writeValueAsString(token);
            for (String next : nextSegments) {
                producer.send(next, updatedToken);
            }

        } catch (Exception e) {
            System.err.println("Fehler in Segment " + segmentId + ": " + e.getMessage());
        }
    }
}
