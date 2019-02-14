package wtf.benedict.kitchen.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@Path("/shelves")
@AllArgsConstructor
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@JBossLog
public class ShelfResource {
  @GET
  public Response getShelves() {
    return Response.status(200).entity("HELLO WORLD").build();
  }
}
