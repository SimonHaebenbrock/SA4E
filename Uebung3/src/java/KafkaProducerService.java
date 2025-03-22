import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Kafka Producer Service zum Senden von Nachrichten an ein Kafka Topic.
 */
public class KafkaProducerService {
    private final KafkaProducer<String, String> producer;

    public KafkaProducerService(String brokers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<>(props);
    }

    public void send(String topic, String message) {
        producer.send(new ProducerRecord<>(topic, message));
        producer.flush();
    }

    public void close() {
        producer.close();
    }
}
