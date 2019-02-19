package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.time.Clock;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.test.TestUtil;

public class OverflowShelfTest {
  @Test
  public void pull_byOrderId() throws Exception {
    val underTest = new OverflowShelf(2, (id, order) -> {});

    val fresh = newOrder(10, HOT, 200);
    val stale = newOrder(11, COLD, 100);

    underTest.put(fresh);
    underTest.put(stale);
    assertEquals(fresh, underTest.pull(HOT, 10));
  }


  @Test
  public void pushAndPullShouldTrackSize() throws Exception {
    val underTest = new OverflowShelf(1, (id, order) -> {});

    val fresh = newOrder(10, HOT, 200);
    val stale = newOrder(11, COLD, 100);

    // Push and pull a few to make sure the size is tracked accurately.
    underTest.put(fresh);
    assertEquals(fresh, underTest.pullStalest(HOT));
    assertNull(underTest.pull(HOT, 10));
    underTest.put(stale);
    assertEquals(stale, underTest.pullStalest(COLD));
    underTest.put(fresh);
    underTest.put(stale);
    assertEquals(fresh, underTest.pullStalest(HOT));
    assertNull(underTest.pullStalest(HOT));
  }


  @Test(expected = StaleOrderException.class)
  public void overflowShouldRejectNewOrderIfItsStalest() throws Exception {
    val underTest = new OverflowShelf(1, (id, order) -> {});

    val fresh = newOrder(10, HOT, 200);
    val stale = newOrder(11, COLD, 100);

    underTest.put(fresh);
    underTest.put(stale);
  }


  @Test
  public void overflowShouldDiscardStalest() throws Exception {
    val underTest = new OverflowShelf(1, (id, order) -> {});

    val fresh = newOrder(10, HOT, 200);
    val stale = newOrder(11, COLD, 100);

    underTest.put(stale);
    underTest.put(fresh);

    assertEquals(fresh, underTest.pullStalest(HOT));
    assertNull(underTest.pullStalest(COLD));
  }


  private static Order newOrder(long id, Temperature temp, int initialShelfLife) {
    return new Order.Builder()
        .id(id)
        .name("name")
        .temp(temp)
        .baseDecayRate(1)
        .initialShelfLife(initialShelfLife)
        .decayStrategy(new CumulativeDecayStrategy(newClock()))
        .build();
  }


  private static Clock newClock() {
    val one = TestUtil.instant(2019, 1, 1, 0, 0, 0);
    val two = TestUtil.instant(2019, 1, 1, 0, 0, 1);

    val clock = mock(Clock.class);
    when(clock.instant()).thenReturn(one, two);
    return clock;
  }
}
