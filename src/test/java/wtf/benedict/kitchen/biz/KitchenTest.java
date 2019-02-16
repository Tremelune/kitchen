package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

public class KitchenTest {
  @Test
  public void orderShouldBeFoundById() {
    val underTest = new Kitchen(mock(CustomerServiceClient.class));
    assertNull(underTest.pickupOrder(1337));

    val order = new Order.Builder()
        .clock(TestUtil.clock(2019, 1, 1, 0, 0, 0))
        .id(10)
        .name("name")
        .temp(HOT)
        .baseDecayRate(1)
        .initialShelfLife(100)
        .build();

    underTest.receiveOrder(order);
    assertEquals(order, underTest.pickupOrder(10));
  }
}
