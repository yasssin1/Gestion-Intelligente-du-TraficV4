package pollution;

import kafka.KafkaProducerUtil;

public class Pollution implements Runnable{

    private String route;
    private int niveau = 0;
    private volatile boolean running = true;

    public Pollution(String route) {
        this.route = route;
    }

    public String getRoute() {
    	return route;
    }
    public int getNiveau() {
    	return niveau;
    }
    public void stopSim() {
    	this.running = false;
    }

	@Override
	public void run() {
		while(running) {
            niveau = (int)(Math.random() * 101);
            KafkaProducerUtil.publish("pollution-data", route,
                    "{\"route\":\"" + route + "\"," +
                    "\"niveau\":" + niveau + "}");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	}
}
