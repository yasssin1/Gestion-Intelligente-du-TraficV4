package kafka;

import org.apache.kafka.clients.consumer.*;
import bruit.BruitMap;
import java.time.Duration;
import java.util.*;

public class KafkaRouteConsumer implements Runnable {
    private volatile boolean running = true;

    public void stop() { running = false; }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "bruit-service");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("routes"));

            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    String value = record.value();
                    if (value.contains("\"action\":\"ADD\"")) {
                        String routeName = record.key();
                        BruitMap.getInstance().addRoute(routeName);
                        System.out.println("Bruit started for: " + routeName);
                    } else if (value.contains("\"action\":\"DELETE\"")) {
                        String routeName = record.key();
                        BruitMap.getInstance().deleteRoute(routeName);
                        System.out.println("Bruit stopped for: " + routeName);
                    }
                }
            }
        }
    }
}
