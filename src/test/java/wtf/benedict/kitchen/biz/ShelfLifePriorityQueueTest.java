package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

public class ShelfLifePriorityQueueTest {
  @Test
  public void emptyQueueShouldReturnNull() {
    val underTest = new ShelfLifePriorityQueue(null, 0);
    assertNull(underTest.pull());
  }


  @Test
  public void fullQueueShouldEvictOrderWithHighestShelfLife() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);

    val orderA = Order.builder()
        .name("a")
        .shelfLife(1)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val orderB = Order.builder()
        .name("b")
        .shelfLife(2)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new ShelfLifePriorityQueue(clock, 1);
    underTest.add(orderB, 1);
    underTest.add(orderA, 1);

    assertEquals("a", underTest.pull().getName());
  }


  @Test
  public void fullQueueShouldNotAddOrderIfItHasTheHighestShelfLife() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);

    val orderA = Order.builder()
        .name("a")
        .shelfLife(1)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val orderB = Order.builder()
        .name("b")
        .shelfLife(2)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new ShelfLifePriorityQueue(clock, 1);
    underTest.add(orderA, 1);
    underTest.add(orderB, 1);

    assertEquals("a", underTest.pull().getName());
  }


  @Test
  public void fullQueueShouldEvictOrderWithHighestShelfLife_multiplier() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1); // We need non-zero decay for the multiplier.

    val orderA = Order.builder()
        .name("a")
        .shelfLife(1)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val orderB = Order.builder()
        .name("b")
        .shelfLife(2)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new ShelfLifePriorityQueue(clock, 1);
    underTest.add(orderB, 3);
    underTest.add(orderA, 1); // Fast decay means B is fresher and should be evicted.

    assertEquals("a", underTest.pull().getName());
  }


  @Test
  public void queueShouldBeInOrderOfShelfLife() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1); // We need non-zero decay for the multiplier.

    val orderA = Order.builder()
        .id(10)
        .shelfLife(1)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val orderB = Order.builder()
        .id(11)
        .shelfLife(2)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val orderC = Order.builder()
        .id(12)
        .shelfLife(1)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val underTest = new ShelfLifePriorityQueue(clock, 3);
    underTest.add(orderB, 1);
    underTest.add(orderC, 1);
    underTest.add(orderA, 3);

    assertEquals(10, underTest.pull().getId());
    assertEquals(11, underTest.pull().getId());
    assertEquals(12, underTest.pull().getId());
  }
}
