package flux;

public class RouteData {
    private RouteThread routeThread;

    public RouteData(RouteThread routeThread) {
        this.routeThread = routeThread;
    }

    public RouteThread getRouteThread() {
        return routeThread;
    }
}