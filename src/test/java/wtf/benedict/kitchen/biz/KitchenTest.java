package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.test.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class KitchenTest {
  @Mock
  private Storage storage;


  @Test
  public void orderShouldBeFoundById() {
    Storage storage = new Storage();
    val underTest = new Kitchen(storage);

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
    assertNull(storage.pull(10));
  }


  @Test(expected = UnsupportedOperationException.class)
  public void rejectedOrderShouldGetRefund() throws Exception {
    val underTest = new Kitchen(storage);
    doThrow(new StaleOrderException(null)).when(storage).put(any());

    underTest.receiveOrder(null);
  }


  @Test
  public void randomShouldAlwaysBeWithinRange() {
    val underTest = new Kitchen(storage);

    // Just try it a bunch and see if it's ever out of range.
    for (int i = 0; i < 1000; i++) {
      int delay = underTest.getPickupDelay();
      assertTrue(delay >= 2000);
      assertTrue(delay <= 10000);
    }
  }
}
