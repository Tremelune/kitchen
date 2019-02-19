package wtf.benedict.kitchen.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import wtf.benedict.kitchen.biz.Kitchen;
import wtf.benedict.kitchen.biz.StorageAggregator.StorageState;

@Path("/kitchens")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@JBossLog
@AllArgsConstructor
public class KitchenResource {
  private final Kitchen kitchen;
  private final OrderGenerator orderGenerator;


  @GET
  public StorageState getShelves() {
    return kitchen.getState();
  }


  @POST
  @Path("/starts")
  public Response start() {
    kitchen.reset();
    orderGenerator.reset();
    orderGenerator.generateOrders(kitchen);
    return Response.status(200).build();
  }
}
