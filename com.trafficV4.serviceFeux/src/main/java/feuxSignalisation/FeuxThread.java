package feuxSignalisation;

import baseDonnees.DBRouteState;
import kafka.KafkaProducerUtil;


public class FeuxThread implements Runnable {
    private String intersection;
    private String route1Name;
    private String route2Name;
    private boolean route1Rouge = true;
    private volatile boolean running = true;
    private Thread thread;

    public FeuxThread(String intersection, String route1Name, String route2Name) {
        this.intersection = intersection;
        this.route1Name = route1Name;
        this.route2Name = route2Name;
    }
    public String getRoute1Name() {
        return route1Name;
    }

    public String getRoute2Name() {
        return route2Name;
    }

    public boolean isRougeFor(String routeName) {
        if (routeName.equals(route1Name)) return route1Rouge;
        if (routeName.equals(route2Name)) return !route1Rouge;
        throw new IllegalArgumentException("Route inconnue: " + routeName);
    }

    public String getIntersection() { return intersection; }

    public void stopFeux() { 
    	running = false;
    	thread.interrupt();
    
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
    	thread = Thread.currentThread();
        int tentative = 0;
        while (running) {
            sleep(60000);

            int flux1 = DBRouteState.getVehicules(route1Name);
            int flux2 = DBRouteState.getVehicules(route2Name);
            if (route1Rouge) {
                if (flux1 <= flux2 && tentative < 3) {

                    KafkaProducerUtil.publish("intersection-adjustment", intersection,
                            "{ \"intersection\":\"" + intersection + "\"," +
                            "\"action\":\"EXTEND_GREEN\"," +
                            "\"route\":\"" + route2Name + "\"," +
                            "\"reason\":\"Higher traffic\"," +
                            "\"flux1\":" + flux1 + "," +
                            "\"flux2\":" + flux2 + " }");

                    sleep(10000);
                    tentative++;
                    continue;
                }
            } else {
                if (flux1 > flux2 && tentative < 3) {

                    KafkaProducerUtil.publish("intersection-adjustment", intersection,
                            "{ \"intersection\":\"" + intersection + "\"," +
                            "\"action\":\"EXTEND_GREEN\"," +
                            "\"route\":\"" + route1Name + "\"," +
                            "\"reason\":\"Higher traffic\"," +
                            "\"flux1\":" + flux1 + "," +
                            "\"flux2\":" + flux2 + " }");

                    sleep(10000);
                    tentative++;
                    continue;
                }
            }
            route1Rouge = !route1Rouge;
            tentative = 0;
        }
    }
}