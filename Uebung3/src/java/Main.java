import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Main Klasse für die Initialisierung des Rennens und Start der Logik.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Zunächst wird der Kafka-Broker und der Pfad zur JSON-Datei festgelegt
        String brokers = "localhost:9092";
        String jsonFilePath = "src/resources/tracks.json";
        KafkaProducerService producer = new KafkaProducerService(brokers);
        ObjectMapper mapper = new ObjectMapper();
        Scanner scanner = new Scanner(System.in);

        // Abfrage der Anzahl der Runden und Start des Rennens
        System.out.print("Enter number of laps: ");
        int maxLaps = scanner.nextInt();
        scanner.nextLine(); // Eingabepuffer leeren

        System.out.println("Press ENTER to start the race...");
        scanner.nextLine();

        try {
            TrackConfigWrapper wrapper = mapper.readValue(new File(jsonFilePath), TrackConfigWrapper.class);
            List<Track> tracks = wrapper.tracks;
            List<String> allVehicleIds = new ArrayList<>();

            // Segmente starten
            for (Track track : tracks) {
                for (SegmentConfig segConfig : track.segments) {
                    SegmentService segmentService = new SegmentService(
                            segConfig.segmentId,
                            segConfig.type,
                            segConfig.nextSegments,
                            brokers,
                            producer
                    );
                    new Thread(segmentService).start();
                }
            }

            // Token pro Strecke erzeugen und verschicken
            for (Track track : tracks) {
                for (SegmentConfig segConfig : track.segments) {
                    // Wenn es sich um ein Start-Ziel-Segment handelt, wird ein Token erzeugt und verschickt
                    if ("start-goal".equals(segConfig.type)) {
                        String vehicleId = "Streitwagen " + track.trackId;
                        String tokenId = UUID.randomUUID().toString();
                        allVehicleIds.add(vehicleId);

                        RaceToken token = new RaceToken(System.currentTimeMillis(), 0, maxLaps, vehicleId);
                        token.tokenId = tokenId;
                        SegmentService.setLatestTokenId(vehicleId, tokenId);

                        String tokenJson = mapper.writeValueAsString(token);
                        System.out.println("Sending token to " + segConfig.segmentId + " for " + vehicleId);
                        producer.send(segConfig.segmentId, tokenJson);
                    }
                }
            }

            // Liste aller erwarteten Streitwagen für Zielprüfung
            SegmentService.setExpectedVehicles(allVehicleIds);

        } catch (Exception e) {
            logger.severe("Fehler beim Starten des Rennens: " + e.getMessage());
        }
    }
}
