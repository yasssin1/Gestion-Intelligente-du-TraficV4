package flux;

import java.util.*;
import jakarta.jws.*;

@WebService(name = "FluxVehicules", targetNamespace = "http://FluxVehicules/")
public interface ServiceFluxVehicules {

	@WebMethod
	public int getNbVehicules(@WebParam(name = "routeName") String routeName);
	@WebMethod
	public boolean addRoute(@WebParam(name = "routeName") String routeName);
	@WebMethod
	public boolean deleteRoute(@WebParam(name = "routeName") String routeName);
	@WebMethod
	public Set<String> listRoutes();
	
}
