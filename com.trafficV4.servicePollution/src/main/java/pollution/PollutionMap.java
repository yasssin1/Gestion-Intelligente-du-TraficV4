package pollution;

import baseDonnees.DBRouteState;
import java.util.HashMap;

public class PollutionMap {

    private static PollutionMap instance = new PollutionMap();
    private HashMap<String, Pollution> pollutions = new HashMap<>();
    private HashMap<String, Thread> monitorThreads = new HashMap<>();

    public static PollutionMap getInstance() { return instance; }
    
    
    public void addRoute(String routeName) {
        if (!pollutions.containsKey(routeName)) {
            Pollution pollution = new Pollution(routeName);
            pollutions.put(routeName, pollution);
            new Thread(pollution).start();

            Thread monitor = new Thread(() -> {
                while (pollutions.containsKey(routeName)) {
                    DBRouteState.updatePollution(routeName, pollution.getNiveau());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return; // ← exits cleanly
                    }
                }
            });
            monitorThreads.put(routeName, monitor); // ← track it
            monitor.start();
        }
    }

    public void deleteRoute(String routeName) {
        Pollution pollution = pollutions.remove(routeName);
        if (pollution != null) {
            pollution.stopSim();
            Thread monitor = monitorThreads.remove(routeName);
            if (monitor != null) monitor.interrupt();
        }
    }

    public Pollution getPollution(String routeName) {
        return pollutions.get(routeName);
    }
    public void loadFromDB() {
        for (String name : baseDonnees.DBRoutes.getAll()) {
            if (!pollutions.containsKey(name)) {
                addRoute(name);
            }
        }
    }
}