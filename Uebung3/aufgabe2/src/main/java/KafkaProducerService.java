import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.*;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Kafka Producer Service zum Senden von Nachrichten an ein Kafka Topic.
 */
public class KafkaProducerService {
    private final KafkaProducer<String, String> producer;
    private final AdminClient adminClient;

    public KafkaProducerService(String brokers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
        this.adminClient = AdminClient.create(props);
    }

    public void send(String topic, String message) {
        producer.send(new ProducerRecord<>(topic, message));
    }

    public void createTopicIfNotExists(String topicName) {
        try {
            if (!adminClient.listTopics().names().get().contains(topicName)) {
                NewTopic topic = new NewTopic(topicName, 1, (short) 1); // replFactor = 1
                adminClient.createTopics(Collections.singleton(topic)).all().get();
                System.out.println("Topic erstellt: " + topicName);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Topic-Erstellung fehlgeschlagen: " + topicName + " – " + e.getMessage());
        }
    }
}
