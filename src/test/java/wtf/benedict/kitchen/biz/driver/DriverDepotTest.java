package wtf.benedict.kitchen.biz.driver;

import static org.junit.Assert.assertEquals;
import static wtf.benedict.kitchen.data.Temperature.HOT;

import java.time.Clock;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import wtf.benedict.kitchen.biz.CumulativeDecayStrategy;
import wtf.benedict.kitchen.biz.delivery.DriverDepot;
import wtf.benedict.kitchen.data.storage.DriverStorage;
import wtf.benedict.kitchen.data.Order;
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

    val storage = new DriverStorage();
    val underTest = new DriverDepot(Clock.systemUTC(), storage, 2, 10);
    underTest.schedulePickup(task, newOrder("ace"));

    assertEquals(1, storage.getAll().size());
    assertEquals("ace", storage.getAll().iterator().next().getOrder().getName());

    // Hang out until the task runs, or more than ten seconds pass (max delay time).
    int attempts = 0;
    while(!ran.get() || attempts++ > 110) {
      Thread.sleep(100);
    }

    assertEquals(0, storage.getAll().size());
  }


  private static Order newOrder(String name) {
    return new Order.Builder()
        .id(10)
        .name(name)
        .temp(HOT)
        .baseDecayRate(1)
        .initialShelfLife(100)
        .decayStrategy(new CumulativeDecayStrategy(TestUtil.clock(2019, 1, 1, 0, 0, 0)))
        .build();
  }
}
