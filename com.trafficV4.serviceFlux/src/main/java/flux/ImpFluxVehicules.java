package flux;

import java.util.Set;

import jakarta.jws.WebService;


@WebService(
	    endpointInterface = "flux.ServiceFluxVehicules",
	    serviceName = "FluxVehicules",
	    portName = "FluxVehiculesPort",
	    targetNamespace = "http://FluxVehicules/"
	)
public class ImpFluxVehicules implements ServiceFluxVehicules{
	
	private RouteMap map = RouteMap.getInstance();


	@Override
	public int getNbVehicules(String routeName) {
		RouteThread route = map.getRoute(routeName);

        if(route == null) {
            return -1;
        }

        return route.getNBVehicule();
	}


	@Override
	public boolean addRoute(String routeName) {
		return map.addRoute(routeName);
	}


	@Override
	public boolean deleteRoute(String routeName) {
		return map.deleteRoute(routeName);
	}


	@Override
	public Set<String> listRoutes() {
		return map.listRoutes();
	}

	
}
