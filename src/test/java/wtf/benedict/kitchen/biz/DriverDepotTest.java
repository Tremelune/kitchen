package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class DriverDepotTest {
  @Test
  public void schedulePickupShouldRemoveWhenComplete() throws Exception {
    val ran = new AtomicBoolean();

    val task = new TimerTask() {
      @Override
      public void run() {
        ran.set(true);
      }
    };

    val underTest = new DriverDepot();
    underTest.schedulePickup(task, newOrder("ace"));

    assertEquals(1, underTest.getState().keySet().size());
    assertEquals("ace", underTest.getState().values().iterator().next().getName());

    // Hang out until the task runs, or more than ten seconds pass (max delay time).
    int attempts = 0;
    while(!ran.get() || attempts++ > 110) {
      Thread.sleep(100);
    }

    assertEquals(0, underTest.getState().keySet().size());
  }


  @Test
  public void randomShouldAlwaysBeWithinRange() {
    val underTest = new DriverDepot();

    // Just try it a bunch and see if it's ever out of range.
    for (int i = 0; i < 1000; i++) {
      int delay = underTest.getPickupDelay();
      assertTrue(delay >= 2000);
      assertTrue(delay <= 10000);
    }
  }


  private static Order newOrder(String name) {
    return new Order.Builder()
        .clock(TestUtil.clock(2019, 1, 1, 0, 0, 0))
        .id(10)
        .name(name)
        .temp(HOT)
        .baseDecayRate(1)
        .initialShelfLife(100)
        .build();
  }
}
