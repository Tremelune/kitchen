package wtf.benedict.kitchen.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import lombok.extern.jbosslog.JBossLog;
import wtf.benedict.kitchen.biz.Kitchen;

@Path("/kitchens")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@JBossLog
public class KitchenResource {
  private final KitchenFactory kitchenFactory;
  private final OrderGenerator orderGenerator;

  // Java servlets tend to be implemented as singletons, and thus it is unwise to put state in one,
  // but for this exercise, there can only be one kitchen "active" at a time. The alternative would
  // be to provide a way to clear the storage...It amounts to the same thing.
  private Kitchen kitchen;


  public KitchenResource(KitchenFactory kitchenFactory, OrderGenerator orderGenerator) {
    this.kitchenFactory = kitchenFactory;
    this.orderGenerator = orderGenerator;
  }


  // TODO Return full state of the system.
  @GET
  public Response getShelves() {
    return Response.status(200).entity("HELLO WORLD").build();
  }


  @POST
  @Path("/starts")
  public Response start() {
    kitchen = kitchenFactory.newKitchen();
    orderGenerator.generateOrders(kitchen);
    return Response.status(200).build();
  }
}
