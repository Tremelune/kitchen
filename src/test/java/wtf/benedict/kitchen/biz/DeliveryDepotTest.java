package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import lombok.val;

public class DeliveryDepotTest {
  @Test
  public void randomShouldAlwaysBeWithinRange() {
    val pickupTasker = mock(PickupTasker.class);
    val underTest = new DeliveryDepot(pickupTasker);

    // Just try it a bunch and see if it's ever out of range.
    for (int i = 0; i < 1000; i++) {
      int delay = underTest.getDelay();
      assertTrue(delay >= 2000);
      assertTrue(delay <= 10000);
    }
  }
}
