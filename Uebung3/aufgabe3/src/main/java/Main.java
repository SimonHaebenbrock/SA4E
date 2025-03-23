import com.fasterxml.jackson.databind.JsonNode;
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
        // Zunächst wird der Kafka-Broker und der Pfad zur JSON-Datei festgelegt
        String brokers = "localhost:39092";
        KafkaProducerService producer = new KafkaProducerService(brokers);
        ObjectMapper mapper = new ObjectMapper();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter number of laps: ");
        int maxLaps = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Press ENTER to start the race...");
        scanner.nextLine();

        try {
            InputStream jsonStream = Main.class.getClassLoader().getResourceAsStream("tracks.json");
            JsonNode root = mapper.readTree(jsonStream);

            JsonNode segmentsNode = root.get("tracks").get(0).get("segments");
            int playerCount = root.get("players").asInt(); // aus JSON-Skript

            List<String> allVehicleIds = new ArrayList<>();

            for (JsonNode segment : segmentsNode) {
                String segmentId = segment.get("segmentId").asText();
                String type = segment.get("type").asText();
                List<String> nextSegments = new ArrayList<>();
                for (JsonNode next : segment.get("nextSegments")) {
                    nextSegments.add(next.asText());
                }

                SegmentService service = new SegmentService(segmentId, type, nextSegments, brokers, producer);
                new Thread(service).start();
            }

            for (int i = 1; i <= playerCount; i++) {
                String vehicleId = "Streitwagen " + i;
                String tokenId = UUID.randomUUID().toString();
                allVehicleIds.add(vehicleId);

                RaceToken token = new RaceToken(System.currentTimeMillis(), 0, maxLaps, vehicleId);
                token.tokenId = tokenId;
                SegmentService.setLatestTokenId(vehicleId, tokenId);

                String tokenJson = mapper.writeValueAsString(token);
                System.out.println("Sending token to start-and-goal for " + vehicleId);
                producer.send("start-and-goal", tokenJson);
            }

            SegmentService.setExpectedVehicles(allVehicleIds);

        } catch (Exception e) {
            System.err.println("Fehler beim Starten des Rennens: " + e.getMessage());
        }
    }
}
