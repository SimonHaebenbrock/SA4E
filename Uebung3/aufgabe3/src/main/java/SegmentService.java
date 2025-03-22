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
import java.util.concurrent.ThreadLocalRandom;
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
        logger.info("Segment " + segmentId + " gestartet (Typ: " + type + ")");
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                processToken(record.value());
            }
        }
    }

    /*
     * Verarbeitung eines Tokens: Prüfen, ob Token gültig ist und ggf. weiterleiten.
     * Hier mit der Unterscheidung der Segment-Typen.
     */
    private void processToken(String tokenJson) {
        try {
            RaceToken token = mapper.readValue(tokenJson, RaceToken.class);
            if (!isValidToken(token)) return;

            switch (type) {
                // Wenn es sich um ein Start-Ziel-Segment handelt, wird der Rundenzähler erhöht
                case "start-goal":
                    token.lapCount++;
                    logger.info(token.vehicleId + " erreicht " + segmentId + ", Runde: " + token.lapCount);

                    // Wenn die maximale Rundenzahl erreicht ist, wird das Rennen beendet
                    if (token.lapCount >= token.maxLaps) {
                        long totalTime = System.currentTimeMillis() - token.startTime;
                        logger.info(token.vehicleId + " hat das Rennen beendet in " + totalTime + " ms");

                        // Prüfen, ob alle Streitwagen im Ziel sind
                        finishedVehicles.add(token.vehicleId);
                        synchronized (raceLock) {
                            if (finishedVehicles.containsAll(expectedVehicles)) {
                                logger.info("Alle Streitwagen sind im Ziel. Rennen beendet.");
                                System.exit(0);
                            }
                        }
                        return;
                    }
                    break;
                // Wenn das Caesar-Segment erreicht wird, wird Caesar gegrüßt
                case "caesar":
                    logger.info(token.vehicleId + " grüßt Caesar am Segment " + segmentId);
                    break;
                // Bei einem Engpass wird eine zufällige Wartezeit zwischen 500 und 1500ms simuliert
                case "bottleneck":
                    int delay = ThreadLocalRandom.current().nextInt(500, 1500);
                    logger.info(token.vehicleId + " steckt im Engpass " + segmentId + ", wartet " + delay + "ms");
                    Thread.sleep(delay);
                    break;
                // Sonst wird der Token an das nächste Segment weitergeleitet
                default:
                    logger.info("Segment " + segmentId + " leitet " + token.vehicleId + " weiter.");
            }

            String updatedToken = mapper.writeValueAsString(token);
            for (String next : nextSegments) {
                producer.send(next, updatedToken);
            }

        } catch (Exception e) {
            logger.severe("Fehler bei Verarbeitung in Segment " + segmentId + ": " + e.getMessage());
        }
    }
}
