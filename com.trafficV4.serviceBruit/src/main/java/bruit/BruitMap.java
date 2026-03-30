package bruit;

import baseDonnees.DBRouteState;
import java.util.HashMap;

public class BruitMap {

    private static BruitMap instance = new BruitMap();
    private HashMap<String, Bruit> bruits = new HashMap<>();
    private HashMap<String, Thread> monitorThreads = new HashMap<>();

    public static BruitMap getInstance() { return instance; }

    public void addRoute(String routeName) {
        if (!bruits.containsKey(routeName)) {
            Bruit bruit = new Bruit(routeName);
            bruits.put(routeName, bruit);
            new Thread(bruit).start();

            Thread monitor = new Thread(() -> {
                while (bruits.containsKey(routeName)) {
                    DBRouteState.updateBruit(routeName, bruit.getNiveau());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
            monitorThreads.put(routeName, monitor);
            monitor.start();
        }
    }

    public void deleteRoute(String routeName) {
        Bruit bruit = bruits.remove(routeName);
        if (bruit != null) {
            bruit.stopSim();
            Thread monitor = monitorThreads.remove(routeName);
            if (monitor != null) monitor.interrupt();
        }
    }

    public Bruit getBruit(String routeName) {
        return bruits.get(routeName);
    }

    public void loadFromDB() {
        for (String name : baseDonnees.DBRoutes.getAll()) {
            if (!bruits.containsKey(name)) {
                addRoute(name);
            }
        }
    }
}
