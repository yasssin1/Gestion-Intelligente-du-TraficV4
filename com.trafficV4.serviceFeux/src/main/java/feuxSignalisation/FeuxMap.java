package feuxSignalisation;

import baseDonnees.DBIntersections;
import baseDonnees.DBRoutes;
import java.util.*;
import kafka.KafkaProducerUtil;

public class FeuxMap {

    private static FeuxMap instance = new FeuxMap();
    private HashMap<String, FeuxThread> intersections = new HashMap<>();

    public static FeuxMap getInstance() { return instance; }

    private void startIntersection(String intersection, String route1Name, String route2Name) {
        FeuxThread feux = new FeuxThread(intersection, route1Name, route2Name);
        intersections.put(intersection, feux);
        new Thread(feux).start();
    }

    public boolean addIntersection(String intersection, String route1Name, String route2Name) {
        if (intersections.containsKey(intersection)) return false;

        // Check DB instead of RouteMap
        List<String> routes = DBRoutes.getAll();
        if (!routes.contains(route1Name) || !routes.contains(route2Name)) return false;

        startIntersection(intersection, route1Name, route2Name);
        DBIntersections.insert(intersection, route1Name, route2Name);
        return true;
    }

    public boolean deleteIntersection(String intersection) {
        FeuxThread feux = intersections.get(intersection);
        if (feux == null) return false;
        feux.stopFeux();
        intersections.remove(intersection);
        DBIntersections.delete(intersection);
        return true;
    }

    public boolean isRougeFor(String intersection, String routeName) {
        FeuxThread feux = intersections.get(intersection);
        if (feux == null) throw new RuntimeException("Intersection inconnue");
        return feux.isRougeFor(routeName);
    }

    public Set<String> listIntersections() {
        return intersections.keySet();
    }

    public void stopAllThreads() {
        for (String name : new HashSet<>(intersections.keySet())) {
            FeuxThread feux = intersections.get(name);
            if (feux != null) feux.stopFeux();
        }

        intersections.clear();

        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }
    
    public void loadFromDB() {
        for (String[] row : DBIntersections.getAll()) {
            String name = row[0], r1 = row[1], r2 = row[2];
            if (!intersections.containsKey(name)) {
                startIntersection(name, r1, r2);
            }
        }
        startKafkaPublisher();
    }
    
    private Thread monitorThread;

    public void startKafkaPublisher() {
    	System.out.println("Kafka publisher running. intersections = " + intersections.size());

        monitorThread = new Thread(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                for (String name : intersections.keySet()) {

                    FeuxThread feux = intersections.get(name);
                    if (feux == null) continue;

                    KafkaProducerUtil.publish(
                    	    "intersection-data",
                    	    name,
                    	    "{"
                    	        + "\"intersection\":\"" + name + "\","
                    	        + "\"route1\":\"" + feux.getRoute1Name() + "\","
                    	        + "\"route2\":\"" + feux.getRoute2Name() + "\","
                    	        + "\"route1Red\":" + feux.isRougeFor(feux.getRoute1Name()) + ","
                    	        + "\"route2Red\":" + feux.isRougeFor(feux.getRoute2Name())
                    	    + "}"
                    	);
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        monitorThread.start();
    }
    
}