package xmaswishes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.io.IOException;

@Component
public class WishProcessorRoute extends RouteBuilder {

    @Autowired
    private WishRepository wishRepository;

    @Override
    public void configure() throws Exception {

        // Monitor the 'wishes' folder for new JSON files
        from("file:Uebung2/Loesung/src/main/resources/wishes?noop=true&include=.*\\.json")
                .routeId("wishProcessorRoute")
                .log("Processing file: ${header.CamelFileName}")
                .process(this::printDirectoryContents) // Print directory contents
                .unmarshal().json(JsonLibrary.Jackson, Wish.class) // Convert JSON to Wish object
                .log("Parsed Wish: ${body}")
                .process(this::saveWishToDatabase) // Save Wish to database
                .log("Wish saved to database: ${body}")
                .end();
    }

    private void printDirectoryContents(Exchange exchange) {
        Path folderPath = Paths.get("Uebung2/Loesung/src/main/resources/wishes");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            System.out.println("Contents of the 'wishes' folder:");
            for (Path entry : stream) {
                System.out.println(entry.getFileName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveWishToDatabase(Exchange exchange) {
        Wish wish = exchange.getIn().getBody(Wish.class);
        wishRepository.save(wish);
    }
}