package flux;

public class RouteThread implements Runnable{

	private int NBVehicule = 50;
	private int max = 50, min = 10;
	private volatile boolean running = true;
	
	public RouteThread() {
		super();
	}
	
	public int getNBVehicule() {
		return NBVehicule;
	}
	
	public void stopRoute() {
		running = false;
	}
	
	@Override
	public void run() {
		while(running) {
            NBVehicule = (int)(Math.random() * (max - min + 1) + min);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	}

}
