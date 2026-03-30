package pollution;

import jakarta.servlet.*;
import kafka.KafkaRouteConsumer;

public class PollutionListener implements ServletContextListener {

    private KafkaRouteConsumer consumer;
    private Thread consumerThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	PollutionMap.getInstance().loadFromDB();
        consumer = new KafkaRouteConsumer();
        consumerThread = new Thread(consumer);
        consumerThread.start();
        System.out.println("Pollution service started, listening to Kafka...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (consumer != null) consumer.stop();
        if (consumerThread != null) consumerThread.interrupt();
    }
}