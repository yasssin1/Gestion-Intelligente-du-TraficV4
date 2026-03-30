package bruit;

import jakarta.servlet.*;
import kafka.KafkaRouteConsumer;

public class BruitListener implements ServletContextListener {

    private KafkaRouteConsumer consumer;
    private Thread consumerThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        BruitMap.getInstance().loadFromDB();
        consumer = new KafkaRouteConsumer();
        consumerThread = new Thread(consumer);
        consumerThread.start();
        System.out.println("Bruit service started, listening to Kafka...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (consumer != null) consumer.stop();
        if (consumerThread != null) consumerThread.interrupt();
    }
}
