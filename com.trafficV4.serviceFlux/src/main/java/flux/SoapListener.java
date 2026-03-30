package flux;


import jakarta.servlet.*; 
import jakarta.xml.ws.Endpoint;
import kafka.KafkaProducerUtil;

public class SoapListener implements ServletContextListener {

	private Endpoint endpoint1;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
	    RouteMap.getInstance().loadFromDB();
	    
	    endpoint1 = Endpoint.publish("http://localhost:6000/ws/FluxVehicules", new ImpFluxVehicules());
	}

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    	RouteMap.getInstance().stopAllThreads();
        if (endpoint1 != null) endpoint1.stop();
        KafkaProducerUtil.close();

    }
}