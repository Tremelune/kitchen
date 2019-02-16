package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static wtf.benedict.kitchen.biz.Order.Temperature.HOT;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

public class TemperatureShelfTest {
  @Test
  public void put() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val overflowShelf = new OverflowShelf(10, clock); // Capacity is arbitrary
    val underTest = new TemperatureShelf(clock, 1, overflowShelf, HOT);

    val order = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    underTest.put(order);

    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void put_shouldOverflowFreshestOntoOverflowShelf() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val overflowShelf = new OverflowShelf(10, clock); // Capacity is arbitrary
    val underTest = new TemperatureShelf(clock, 1, overflowShelf, HOT);

    val fresh = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(1000)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val stale = Order.builder()
        .id(11)
        .temp(HOT)
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val freshest = Order.builder()
        .id(12)
        .temp(HOT)
        .shelfLife(2000)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    underTest.put(fresh);
    underTest.put(stale);
    underTest.put(freshest);

    assertEquals(fresh, overflowShelf.pullStalest(HOT));
    assertEquals(freshest, overflowShelf.pullStalest(HOT));
  }


  @Test
  public void pull() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val overflowShelf = new OverflowShelf(10, clock); // Capacity is arbitrary
    val underTest = new TemperatureShelf(clock, 1, overflowShelf, HOT);

    val order = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    assertNull(underTest.pull(10));
    underTest.put(order);

    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void pull_pullsFromOverflow() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val overflowShelf = new OverflowShelf(10, clock); // Capacity is arbitrary
    val underTest = new TemperatureShelf(clock, 1, overflowShelf, HOT);

    val fresh = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(1000)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    assertNull(underTest.pull(10));
    overflowShelf.put(fresh);
    assertEquals(fresh, underTest.pull(10));
  }


  @Test
  public void pull_pullsFromOverflowWhenSpaceBecomesAvailable() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val overflowShelf = new OverflowShelf(10, clock); // Capacity is arbitrary
    val underTest = new TemperatureShelf(clock, 1, overflowShelf, HOT);

    val order = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(1000)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val overflow = Order.builder()
        .id(11)
        .temp(HOT)
        .shelfLife(1000)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    underTest.put(order);
    overflowShelf.put(overflow);

    assertEquals(order, underTest.pull(10));
    assertNull(overflowShelf.pull(HOT, 11));
    assertEquals(overflow, underTest.pull(11));
  }
}
