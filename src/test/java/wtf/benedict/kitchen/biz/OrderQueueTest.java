package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.OrderQueue.OverflowException;
import wtf.benedict.kitchen.test.TestUtil;

public class OrderQueueTest {
  @Test(expected = OverflowException.class)
  public void add_capacityCheck() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);
    val underTest = new OrderQueue(clock, 1, 1);
    underTest.put(newOrder(), 1);
    underTest.put(newOrder(), 1);
  }


  @Test(expected = OverflowException.class)
  public void pull() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);

    val freshOrder = Order.builder()
        .name("fresh")
        .decayRate(1)
        .shelfLife(2)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .name("stale")
        .decayRate(1)
        .shelfLife(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new OrderQueue(clock, 1, 1);
    underTest.put(freshOrder, 1);
    underTest.put(staleOrder, 1);

    assertEquals("stale", underTest.pull().getName());
    assertEquals("fresh", underTest.pull().getName());
    assertNull(underTest.pull());
  }


  @Test(expected = OverflowException.class)
  public void pull_byId() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);

    val freshOrder = Order.builder()
        .id(10)
        .name("fresh")
        .decayRate(1)
        .shelfLife(2)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .id(11)
        .name("stale")
        .decayRate(1)
        .shelfLife(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new OrderQueue(clock, 1, 1);
    underTest.put(staleOrder, 1);
    underTest.put(freshOrder, 1);

    assertNull(underTest.pull(1337)); // Never existed...
    assertEquals("fresh", underTest.pull(10).getName());
    assertNull(underTest.pull(10));
    assertEquals("stale", underTest.pull(11).getName());
    assertNull(underTest.pull(11));
  }


  @Test(expected = OverflowException.class)
  public void peek() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);

    val freshOrder = Order.builder()
        .name("fresh")
        .decayRate(1)
        .shelfLife(2)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .name("stale")
        .decayRate(1)
        .shelfLife(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new OrderQueue(clock, 1, 1);
    underTest.put(staleOrder, 1);
    underTest.put(freshOrder, 1);

    assertEquals("stale", underTest.peek().getName());
    assertEquals("stale", underTest.pull().getName());
    assertEquals("fresh", underTest.pull().getName());
  }


  @Test
  public void ordering() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1); // We need some time to have passed for decay.

    val freshOrder = Order.builder()
        .id(10)
        .name("fresh")
        .decayRate(1)
        .shelfLife(300)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val fastDecayOrder = Order.builder()
        .id(11)
        .name("slowDecay")
        .decayRate(.1)
        .shelfLife(100)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .id(12)
        .name("stale")
        .decayRate(.9)
        .shelfLife(100)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val multiplierOrder = Order.builder()
        .id(13)
        .name("multiplier")
        .decayRate(1)
        .shelfLife(200)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new OrderQueue(clock, 4, 1);
    underTest.put(freshOrder, 1);
    underTest.put(fastDecayOrder, 1);
    underTest.put(staleOrder, 1);
    underTest.put(multiplierOrder, 1.5);

    assertEquals("stale", underTest.pull().getName());
    assertEquals("slowDecay", underTest.pull().getName());
    assertEquals("multiplier", underTest.pull().getName());
    assertEquals("fresh", underTest.pull().getName());
  }


  @Test
  public void expiration() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1); // We need some time to have passed for decay.

    val fresh = Order.builder()
        .id(10)
        .name("fresh2deth")
        .decayRate(1)
        .shelfLife(3)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val stale = Order.builder()
        .id(11)
        .name("oldNbusted")
        .decayRate(1)
        .shelfLife(2)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new OrderQueue(clock, 2, 1);
    underTest.put(stale, 1);
    underTest.put(fresh, 1);

    assertEquals("oldNbusted", underTest.peek().getName());
    Thread.sleep(1000);
    assertEquals("fresh2deth", underTest.peek().getName());
    Thread.sleep(1000);
    assertNull(underTest.peek());
  }


  private static Order newOrder() {
    return Order.builder()
        .decayRate(1)
        .shelfLife(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();
  }
}
