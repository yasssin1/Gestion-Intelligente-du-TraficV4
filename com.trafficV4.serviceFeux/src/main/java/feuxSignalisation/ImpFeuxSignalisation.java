package feuxSignalisation;

import jakarta.jws.WebService;
import java.util.Set;

@WebService(
    endpointInterface = "feuxSignalisation.ServiceFeuxSignalisation",
    serviceName = "FeuxSignalisation",
    portName = "FeuxSignalisationPort",
    targetNamespace = "http://FeuxSignalisation/"
)
public class ImpFeuxSignalisation implements ServiceFeuxSignalisation {

    private FeuxMap map = FeuxMap.getInstance();

    @Override
    public boolean addIntersection(String intersection, String route1, String route2) {
        return map.addIntersection(intersection, route1, route2);
    }

    @Override
    public boolean deleteIntersection(String intersection) {
        return map.deleteIntersection(intersection);
    }

    @Override
    public boolean isRougeFor(String intersection, String routeName) {
        return map.isRougeFor(intersection, routeName);
    }

    @Override
    public Set<String> listIntersections() {
        return map.listIntersections();
    }
}