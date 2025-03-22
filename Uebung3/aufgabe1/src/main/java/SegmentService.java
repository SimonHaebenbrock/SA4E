import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * SegmentService Klasse für die Verarbeitung von Tokens in einem Segment.
 */
public class SegmentService implements Runnable {
    private static final Logger logger = Logger.getLogger(SegmentService.class.getName());

    // Tracking der gültigen Tokens pro Streitwagen
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

    // Instanzvariablen
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

        // Kafka Consumer initialisieren
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", "group-" + segmentId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        this.consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(segmentId));
    }

    // Logik für die Verarbeitung von Tokens. Abfragen via poll und anschließendes Verarbeiten
    @Override
    public void run() {
        System.out.println("Segment " + segmentId + " started.");
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    processToken(record.value());
                }
            } catch (Exception e) {
                logger.warning("Fehler beim Polling in Segment " + segmentId + ": " + e.getMessage());
            }
        }
    }

    /*
     * Verarbeitung eines Tokens: Prüfen, ob Token gültig ist und ggf. weiterleiten.
     */
    private void processToken(String tokenJson) {
        try {
            RaceToken token = mapper.readValue(tokenJson, RaceToken.class);

            if (!isValidToken(token)) {
                return; // veralteter Token
            }

            // Wenn es sich um ein Start-Ziel-Segment handelt, wird der Rundenzähler erhöht
            if ("start-goal".equals(type)) {
                token.lapCount++;
                System.out.println(token.vehicleId + " erreicht " + segmentId + ", Runde: " + token.lapCount);

                // Wenn die maximale Rundenzahl erreicht ist, wird das Rennen beendet
                if (token.lapCount >= token.maxLaps) {
                    long elapsed = System.currentTimeMillis() - token.startTime;
                    System.out.println(token.vehicleId + " hat das Rennen beendet in " + elapsed + " ms");
                    finishedVehicles.add(token.vehicleId);

                    // Prüfen, ob alle Streitwagen im Ziel sind
                    synchronized (raceLock) {
                        if (finishedVehicles.containsAll(expectedVehicles)) {
                            System.out.println("Alle Streitwagen sind im Ziel. Rennen beendet.");
                            System.exit(0);
                        }
                    }
                    return;
                }
                // Sonst wird der Token an das nächste Segment weitergeleitet
            } else {
                System.out.println("Segment " + segmentId + " leitet " + token.vehicleId + " weiter.");
            }

            String updatedToken = mapper.writeValueAsString(token);
            for (String next : nextSegments) {
                producer.send(next, updatedToken);
            }

        } catch (Exception e) {
            logger.warning("Fehler beim Verarbeiten von Token im Segment " + segmentId + ": " + e.getMessage());
        }
    }
}
