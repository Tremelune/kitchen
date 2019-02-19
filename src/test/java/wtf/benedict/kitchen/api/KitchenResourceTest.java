package wtf.benedict.kitchen.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.biz.Temperature.HOT;
import static wtf.benedict.kitchen.test.TestUtil.asList;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.gson.Gson;

import io.dropwizard.testing.junit.ResourceTestRule;
import lombok.val;
import wtf.benedict.kitchen.biz.DriverDepot.Delivery;
import wtf.benedict.kitchen.biz.Kitchen;
import wtf.benedict.kitchen.biz.StorageAggregator;
import wtf.benedict.kitchen.biz.StorageAggregator.StorageState;

public class KitchenResourceTest {
  private final Kitchen kitchen = mock(Kitchen.class);
  private final OrderGenerator orderGenerator = mock(OrderGenerator.class);

  private final KitchenResource underTest = new KitchenResource(kitchen, orderGenerator);


  @Rule
  public final ResourceTestRule rule = ResourceTestRule.builder().addResource(underTest).build();


  @Before
  public void setup() {
    // TODO Bother with this?
//    ObjectMapperConfigurer.configure(rule.getObjectMapper());
  }


  @Test
  public void getState() {
    val entry = StorageAggregator.Entry.builder()
        .name("chicken")
        .temp(HOT)
        .remainingShelfLife(100)
        .build();

    val state = StorageState.builder()
        .hotEntries(asList(entry))
        .deliveries(asList(new Delivery("meatball", 150)))
        .build();

    when(kitchen.getState()).thenReturn(state);

    Response response = rule.target("/kitchens")
        .request()
        .get();

    assertEquals(200, response.getStatus());
    val responseJson = response.readEntity(String.class);
    val responseState = new Gson().fromJson(responseJson, StorageState.class);
    assertEquals(1, responseState.getHotEntries().size());
    assertEquals("chicken", responseState.getHotEntries().get(0).getName());
    assertEquals(HOT, responseState.getHotEntries().get(0).getTemp());
    assertEquals(100, responseState.getHotEntries().get(0).getRemainingShelfLife());
    assertEquals(1, responseState.getDeliveries().size());
    assertEquals("meatball", responseState.getDeliveries().get(0).getName());
    assertEquals(150, responseState.getDeliveries().get(0).getSecondsUntilPickup());
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
