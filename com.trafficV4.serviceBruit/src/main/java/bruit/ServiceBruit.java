package bruit;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/bruit")
public class ServiceBruit {

    private BruitMap map = BruitMap.getInstance();

    @GET
    @Path("/{route}")
    @Produces(MediaType.APPLICATION_JSON)
    public Bruit getBruit(@PathParam("route") String route) {
        Bruit bruit = map.getBruit(route);

        if (bruit == null) {
            throw new NotFoundException("Route not found");
        }
        return bruit;
    }
}
