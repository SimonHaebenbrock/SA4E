import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class KafkaProducerService {
    private final KafkaProducer<String, String> producer;
    private final AdminClient adminClient;

    // Konfigurierbare Werte
    private final int partitions = 3;
    private final short replicationFactor = 3;

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
            // Prüfe, ob Topic bereits existiert
            Set<String> existingTopics = adminClient.listTopics().names().get();

            if (!existingTopics.contains(topicName)) {
                NewTopic topic = new NewTopic(topicName, partitions, replicationFactor);
                adminClient.createTopics(Collections.singleton(topic)).all().get();
                System.out.println("Topic erstellt: " + topicName +
                        " (Partitions: " + partitions + ", RF: " + replicationFactor + ")");
            } else {
                // Optional: Topic-Details anzeigen
                DescribeTopicsResult result = adminClient.describeTopics(Collections.singleton(topicName));
                TopicDescription desc = result.values().get(topicName).get();
                System.out.println("Topic existiert bereits: " + topicName +
                        " (Partitions: " + desc.partitions().size() +
                        ", Replicas: " + desc.partitions().get(0).replicas().size() + ")");
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Fehler bei Topic-Erstellung: " + topicName + " – " + e.getMessage());
        }
    }

    public void close() {
        producer.close();
        adminClient.close();
    }
}
