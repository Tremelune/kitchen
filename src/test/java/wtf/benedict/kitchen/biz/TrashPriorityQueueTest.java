package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.TrashPriorityQueue.QueueOverFlowException;
import wtf.benedict.kitchen.test.TestUtil;

public class TrashPriorityQueueTest {
  @Test(expected = NullPointerException.class)
  public void order() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 1, 1, 1);

    val orderA = Order.builder()
        .name("a")
        .shelfLife(100)
        .decayRate(1)
        .build();

    val orderB = Order.builder()
        .name("b")
        .shelfLife(50)
        .decayRate(1)
        .build();

    val orderC = Order.builder()
        .name("c")
        .shelfLife(10)
        .decayRate(1)
        .build();

    val queue = new TrashPriorityQueue(clock, 2);
    queue.add(orderB);
    queue.add(orderA);
    queue.add(orderC);

    // From lowest shelf life to highest
    assertEquals("c", queue.pull().getName());
    assertEquals("b", queue.pull().getName());
    assertEquals("a", queue.pull().getName());
  }


  @Test(expected = NullPointerException.class)
  public void nullsShouldBeRejected() throws Exception {
    val queue = new TrashPriorityQueue(null, 1);
    queue.add(null);
  }


  @Test(expected = QueueOverFlowException.class)
  public void explodeOnOverflow() throws Exception {
    val queue = new TrashPriorityQueue(null, 1);
    queue.add(newOrder());
    queue.add(newOrder());
  }


  @Test
  public void emptyQueueReturnsNull() {
    val queue = new TrashPriorityQueue(null, 1);
    assertNull(queue.pull());
  }


  private static Order newOrder() {
    return Order.builder()
        .shelfLife(100)
        .decayRate(1)
        .build();
  }
}
