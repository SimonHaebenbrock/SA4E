package xmaswishes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WishFileGenerator {
    public static void main(String[] args) {
        // Definiere das Zielverzeichnis
        File wishesDir = new File("src/main/resources/wishes");

        // Stelle sicher, dass das Verzeichnis existiert
        if (!wishesDir.exists()) {
            wishesDir.mkdirs();  // Erstelle das Verzeichnis, wenn es noch nicht existiert
        }

        String[] wishes = {
                "{\"name\": \"New Wish\", \"description\": \"A Christmas wish for the North Pole\", \"status\": \"pending\"}",
                "{\"name\": \"Another Wish\", \"description\": \"A second Christmas wish for Santa\", \"status\": \"pending\"}"
        };

        for (int i = 0; i < wishes.length; i++) {
            try (FileWriter writer = new FileWriter("src/main/resources/wishes/wish" + (i+1) + ".json")) {
                writer.write(wishes[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
