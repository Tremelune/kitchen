package wtf.benedict.kitchen.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.dropwizard.testing.junit.ResourceTestRule;

public class KitchenResourceTest {
  private final KitchenFactory kitchenFactory = mock(KitchenFactory.class);
  private final OrderGenerator orderGenerator = mock(OrderGenerator.class);

  private final KitchenResource underTest = new KitchenResource(kitchenFactory, orderGenerator);


  @Rule
  public final ResourceTestRule rule = ResourceTestRule.builder().addResource(underTest).build();


  @Before
  public void setup() {
//    ObjectMapperConfigurer.configure(rule.getObjectMapper());
  }


  @Test
  public void getShelves() {
    Response response = rule.target("/kitchens")
        .request()
        .get();

    assertEquals(200, response.getStatus());
  }


  @Test
  public void start() {
    Response response = rule.target("/kitchens/starts")
        .request()
        .post(Entity.json("{}"));

    assertEquals(200, response.getStatus());
    verify(orderGenerator, times(1)).generateOrders(any());
  }
}
