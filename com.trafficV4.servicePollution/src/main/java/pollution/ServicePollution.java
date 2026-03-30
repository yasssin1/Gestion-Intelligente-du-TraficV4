package pollution;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/pollution")
public class ServicePollution {
	
	private PollutionMap map = PollutionMap.getInstance();
	
    @GET
    @Path("/{route}")
    @Produces(MediaType.APPLICATION_JSON)
    public Pollution getPollution(@PathParam("route") String route) {
        Pollution pollution = map.getPollution(route);

        if(pollution == null) {
            throw new NotFoundException("Route not found");
        }
        return pollution;
    }
}