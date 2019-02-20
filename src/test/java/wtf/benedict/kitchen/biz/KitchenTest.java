package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.util.ArrayList;

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
  private DriverDepot driverDepot;
  @Mock
  private Storage storage;


  @Test
  public void orderShouldBeFoundById() {
    Storage storage = new Storage((id, order) -> {}, new ArrayList<>());
    val underTest = new Kitchen(driverDepot, null, (expirationListener, trashedOrders) -> storage);

    assertNull(underTest.pickupOrder(1337));

    val order = new Order.Builder()
        .id(10)
        .name("name")
        .temp(HOT)
        .baseDecayRate(1)
        .initialShelfLife(100)
        .decayStrategy(new CumulativeDecayStrategy(TestUtil.clock(2019, 1, 1, 0, 0, 0)))
        .build();

    underTest.receiveOrder(order);
    assertEquals(order, underTest.pickupOrder(10));
    assertNull(storage.pull(10));
  }


  @Test(expected = IllegalArgumentException.class)
  public void rejectedOrderShouldGetRefund() throws Exception {
    val underTest = new Kitchen(driverDepot, null, (expirationListener, trashedOrders) -> storage);
    doThrow(new StaleOrderException(null)).when(storage).put(any());

    underTest.receiveOrder(null);
  }
}
