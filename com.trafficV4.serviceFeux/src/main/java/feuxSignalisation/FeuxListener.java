package feuxSignalisation;

import jakarta.servlet.*;
import jakarta.xml.ws.Endpoint;

public class FeuxListener implements ServletContextListener {

    private Endpoint endpoint;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        FeuxMap.getInstance().loadFromDB();
        endpoint = Endpoint.publish("http://localhost:6001/ws/FeuxSignalisation", new ImpFeuxSignalisation());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        FeuxMap.getInstance().stopAllThreads();
        if (endpoint != null) endpoint.stop();
    }
}