package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.test.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class KitchenTest {
  @Mock
  private CustomerServiceClient customerServiceClient;
  @Mock
  private DeliveryDepot deliveryDepot;
  @Mock
  private Storage storage;


  @Test
  public void orderShouldBeFoundById() {
    val underTest = new Kitchen(customerServiceClient, deliveryDepot, new Storage());
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

    val orderId = ArgumentCaptor.forClass(Long.class);
    verify(deliveryDepot, times(1)).dispatchDriver(orderId.capture());
    assertEquals(10, (long) orderId.getValue());
  }


  @Test
  public void rejectedOrderShouldGetRefund() throws Exception {
    val underTest = new Kitchen(customerServiceClient, deliveryDepot, storage);
    doThrow(new StaleOrderException(null)).when(storage).put(any());

    val order = mock(Order.class);
    when(order.getId()).thenReturn(10L);

    underTest.receiveOrder(order);

    val orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(customerServiceClient, times(1)).refund(orderCaptor.capture());

    assertEquals(10, orderCaptor.getValue().getId());
  }
}
