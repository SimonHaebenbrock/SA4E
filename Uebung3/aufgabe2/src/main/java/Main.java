import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Main Klasse für die Initialisierung des Rennens und Start der Logik.
 */
public class Main {
    public static void main(String[] args) {
        // Hier werden nun die Broker für die drei Kafka-Instanzen festgelegt
        String brokers = "localhost:29092,localhost:29093,localhost:29094";
        KafkaProducerService producer = new KafkaProducerService(brokers);
        ObjectMapper mapper = new ObjectMapper();
        Scanner scanner = new Scanner(System.in);

        // Abfrage der Anzahl der Runden und Start des Rennens
        System.out.print("Enter number of laps: ");
        int maxLaps = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Press ENTER to start the race...");
        scanner.nextLine();

        try {
            // Einlesen der Konfiguration der Strecke
            InputStream jsonStream = Main.class.getClassLoader().getResourceAsStream("tracks.json");
            TrackConfigWrapper wrapper = mapper.readValue(jsonStream, TrackConfigWrapper.class);
            List<Track> tracks = wrapper.tracks;
            List<String> allVehicleIds = new ArrayList<>();

            for (Track track : tracks) {
                for (SegmentConfig segConfig : track.segments) {
                    // Anlegen des Topics, falls es noch nicht existiert
                    producer.createTopicIfNotExists(segConfig.segmentId);
                }
            }

            // Starte alle Segmente (Threads)
            for (Track track : tracks) {
                for (SegmentConfig segConfig : track.segments) {
                    SegmentService segmentService = new SegmentService(
                            segConfig.segmentId,
                            segConfig.type,
                            segConfig.nextSegments,
                            brokers,
                            producer
                    );
                    // Starten des Threads für das Segment
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
            System.err.println("Fehler beim Starten des Rennens: " + e.getMessage());
        }
    }
}
