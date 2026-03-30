package kafka;

import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class KafkaProducerUtil {

    private static KafkaProducer<String, String> producer;

    static {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public static void publish(String topic, String key, String value) {
        producer.send(new ProducerRecord<>(topic, key, value));
    }

    public static void close() {
        producer.close();
    }
}
