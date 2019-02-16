package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.time.Clock;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.OrderQueue.OverflowException;
import wtf.benedict.kitchen.test.TestUtil;

public class OrderQueueTest {
  @Test(expected = OverflowException.class)
  public void add_capacityCheck() throws Exception {
    val a = newOrder(10, 200);
    val b = newOrder(11, 100);

    val underTest = new OrderQueue(1, 1);

    underTest.put(a);
    underTest.put(b);
  }


  @Test(expected = OverflowException.class)
  public void pullStalest() throws Exception {
    val fresh = newOrder(10, 200);
    val stale = newOrder(11, 100);

    val underTest = new OrderQueue(1, 1);
    underTest.put(fresh);
    underTest.put(stale);

    assertEquals(stale, underTest.pullStalest());
    assertEquals(fresh, underTest.pullStalest());
    assertNull(underTest.pullStalest());
  }


  @Test(expected = OverflowException.class)
  public void pull_byId() throws Exception {
    val fresh = newOrder(10, 200);
    val stale = newOrder(11, 100);

    val underTest = new OrderQueue(1, 1);
    underTest.put(stale);
    underTest.put(fresh);

    assertNull(underTest.pull(1337)); // Never existed...
    assertEquals(fresh, underTest.pull(10));
    assertNull(underTest.pull(10));
    assertEquals(stale, underTest.pull(11));
    assertNull(underTest.pull(11));
  }


  @Test(expected = OverflowException.class)
  public void peek() throws Exception {
    val fresh = newOrder(10, 200);
    val stale = newOrder(12, 100);

    val underTest = new OrderQueue(1, 1);
    underTest.put(stale);
    underTest.put(fresh);

    assertEquals(stale, underTest.peekStalest());
    assertEquals(stale, underTest.pullStalest());
    assertEquals(fresh, underTest.pullStalest());
  }


//  @Test // TODO I think TreeSort won't do it, 'cause the sort value changes over time. Gotta sort then pull.
  public void ordering() throws Exception {
    val fresh = newOrder(10, 300);
    val stale = newOrder(11, 100);

    val fastDecay = new Order.Builder()
        .clock(clock())
        .id(12)
        .name("fastDecay")
        .temp(HOT)
        .baseDecayRate(10)
        .initialShelfLife(100)
        .build();

    val underTest = new OrderQueue(3, 1);
    underTest.put(stale);
    underTest.put(fastDecay);
    underTest.put(fresh);

    assertEquals(fastDecay, underTest.pullStalest());
    assertEquals(stale, underTest.pullStalest());
    assertEquals(fresh, underTest.pullStalest());
  }


  @Test
  public void expiration() throws Exception {
    val fresh = newOrder(10, 2);
    val stale = newOrder(11, 1);

    val underTest = new OrderQueue(2, 1);
    underTest.put(stale);
    underTest.put(fresh);

    // PassiveExpiringMap uses the system time, so we gotta wait in real life.
    assertEquals(stale, underTest.peekStalest());
    Thread.sleep(1000);
    assertEquals(fresh, underTest.peekStalest());
    Thread.sleep(1000);
    assertNull(underTest.peekStalest());
  }


  private static Order newOrder(long id, int initialShelfLife) {
    return new Order.Builder()
        .clock(clock())
        .id(id)
        .name("name")
        .temp(HOT)
        .baseDecayRate(1)
        .initialShelfLife(initialShelfLife)
        .build();
  }


  private static Clock clock() {
    val one = TestUtil.instant(2019, 1, 1, 0, 0, 0);
    val two = TestUtil.instant(2019, 1, 1, 0, 0, 1);

    val clock = mock(Clock.class);
    when(clock.instant()).thenReturn(one, two);
    return clock;
  }
}
