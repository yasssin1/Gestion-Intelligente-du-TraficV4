package flux;

import java.util.*;

import baseDonnees.DBRouteState;
import baseDonnees.DBRoutes;
import kafka.KafkaProducerUtil;

public class RouteMap {
	
	private static RouteMap instance = new RouteMap();
	private HashMap<String, RouteData> routes;
    private HashMap<String, Thread> monitorThreads = new HashMap<>();
    
	public RouteMap() {
		routes = new HashMap<>();
	}

    public static RouteMap getInstance() {
        return instance;
    }
	


    private void startRoute(String routeName) {
        RouteThread route = new RouteThread();
        routes.put(routeName, new RouteData(route));
        new Thread(route).start();

        DBRouteState.initRoute(routeName);

        Thread monitor = new Thread(() -> {
            while (routes.containsKey(routeName)) {
                DBRouteState.updateVehicules(routeName, route.getNBVehicule());
                KafkaProducerUtil.publish("traffic-data", routeName,
                        "{\"route\":\"" + routeName + "\"," +
                        "\"nbVehicules\":" + route.getNBVehicule() + "}");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // ← restore flag and exit
                    return;
                }
            }
        });
        monitorThreads.put(routeName, monitor);
        monitor.start();
    }

    public boolean addRoute(String routeName) {
        if (!routes.containsKey(routeName)) {
            startRoute(routeName);
            DBRoutes.insert(routeName);
            
            KafkaProducerUtil.publish("routes", routeName, 
                    "{\"action\":\"ADD\",\"route\":\"" + routeName + "\"}");
            
            return true;
        }
        return false;
    }
	
    public RouteThread getRoute(String routeName) {

        RouteData data = routes.get(routeName);
        if(data == null) return null;

        return data.getRouteThread();
    }
	


    
	public Set<String> listRoutes() {
	    return routes.keySet();
	}
	
	public boolean deleteRoute(String routeName) {
	    RouteData data = routes.get(routeName);
	    if (data != null) {
	        data.getRouteThread().stopRoute();

	        Thread monitor = monitorThreads.remove(routeName);
	        if (monitor != null) monitor.interrupt();
	        routes.remove(routeName);
	        DBRoutes.delete(routeName);
	        DBRouteState.deleteRoute(routeName);
	        KafkaProducerUtil.publish("routes", routeName,
	                "{\"action\":\"DELETE\",\"route\":\"" + routeName + "\"}");
	        return true;
	    }
	    System.err.println("route doesn't exist");
	    return false;
	}
	public void shutdown() {
	    new HashSet<>(routes.keySet()).forEach(this::deleteRoute);
	}
	
	public void stopAllThreads() {
	    for (String routeName : new HashSet<>(routes.keySet())) {
	        RouteData data = routes.get(routeName);
	        if (data != null) data.getRouteThread().stopRoute();

	        Thread monitor = monitorThreads.remove(routeName);
	        if (monitor != null) monitor.interrupt();
	    }
	    routes.clear();
	}
	
	//a lancement pour lire la base donnee
	public void loadFromDB() {
	    for (String name : DBRoutes.getAll()) {
	        if (!routes.containsKey(name)) {
	            startRoute(name); // ← no DB write
	        }
	    }
	}
}
