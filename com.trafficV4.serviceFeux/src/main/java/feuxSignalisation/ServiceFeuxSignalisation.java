package feuxSignalisation;

import jakarta.jws.*;
import java.util.Set;

@WebService(name = "FeuxSignalisation", targetNamespace = "http://FeuxSignalisation/")
public interface ServiceFeuxSignalisation {

    @WebMethod
    public boolean addIntersection(@WebParam(name = "intersection") String intersection,
                                   @WebParam(name = "route1") String route1,
                                   @WebParam(name = "route2") String route2);

    @WebMethod
    public boolean deleteIntersection(@WebParam(name = "intersection") String intersection);

    @WebMethod
    public boolean isRougeFor(@WebParam(name = "intersection") String intersection,
                              @WebParam(name = "routeName") String routeName);

    @WebMethod
    public Set<String> listIntersections();
}