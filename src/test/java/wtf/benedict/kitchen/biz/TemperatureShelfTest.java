package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.time.Clock;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

public class TemperatureShelfTest {
  @Test
  public void put() throws Exception {
    val overflowShelf = newOverflowShelf();
    val underTest = newTemperatureShelf(overflowShelf);

    val order = newOrder(10, 100);

    underTest.put(order);

    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void put_shouldOverflowFreshestOntoOverflowShelf() throws Exception {
    val overflowShelf = newOverflowShelf();
    val underTest = newTemperatureShelf(overflowShelf);

    val fresh = newOrder(10, 1000);
    val stale = newOrder(11, 100);
    val freshest = newOrder(12, 2000);

    underTest.put(fresh);
    underTest.put(stale);
    underTest.put(freshest);

    assertEquals(fresh, overflowShelf.pullStalest(HOT));
    assertEquals(freshest, overflowShelf.pullStalest(HOT));
  }


  @Test
  public void pull() throws Exception {
    val overflowShelf = newOverflowShelf();
    val underTest = newTemperatureShelf(overflowShelf);

    val order = newOrder(10, 100);

    assertNull(underTest.pull(10));
    underTest.put(order);

    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void pull_pullsFromOverflow() throws Exception {
    val overflowShelf = newOverflowShelf();
    val underTest = newTemperatureShelf(overflowShelf);

    val order = newOrder(10, 1000);

    assertNull(underTest.pull(10));
    overflowShelf.put(order);
    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void pull_pullsFromOverflowWhenSpaceBecomesAvailable() throws Exception {
    val overflowShelf = newOverflowShelf();
    val underTest = newTemperatureShelf(overflowShelf);

    val order = newOrder(10, 1000);
    val overflow = newOrder(11, 1000);

    underTest.put(order);
    overflowShelf.put(overflow);

    assertEquals(order, underTest.pull(10));
    assertNull(overflowShelf.pull(HOT, 11));
    assertEquals(overflow, underTest.pull(11));
  }


  private static Order newOrder(long id, int initialShelfLife) {
    return new Order.Builder()
        .clock(newClock())
        .id(id)
        .name("name")
        .temp(HOT)
        .baseDecayRate(1)
        .initialShelfLife(initialShelfLife)
        .build();
  }


  private static Clock newClock() {
    val one = TestUtil.instant(2019, 1, 1, 0, 0, 0);
    val two = TestUtil.instant(2019, 1, 1, 0, 0, 1);

    val clock = mock(Clock.class);
    when(clock.instant()).thenReturn(one, two);
    return clock;
  }


  private static OverflowShelf newOverflowShelf() {
    // Capacity is arbitrary, but enough for tests
    return new OverflowShelf(10, (id, order) -> {});
  }

  private static TemperatureShelf newTemperatureShelf(OverflowShelf overflowShelf) {
    return new TemperatureShelf(1, overflowShelf, HOT, (id, order) -> {});
  }
}
