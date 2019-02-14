package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import lombok.val;

/** A queue in order of shelf life. Orders that are nearest to expiration get pulled first. */
class TrashPriorityQueue {
  private final int capacity;
  private final PriorityQueue<Order> queue;


  TrashPriorityQueue(Clock clock, int capacity) {
    this.capacity = capacity;
    queue = new PriorityQueue<>(capacity, newDecayComparator(clock));
  }


  // We synchronize in case something is added between the check and the add.
  synchronized void add(Order order) throws QueueOverFlowException {
    if (queue.size() >= capacity) {
      throw new QueueOverFlowException(capacity);
    }

    queue.add(order);
  }


  /** If the queue is empty, this will return null. I just feel like it makes more sense. */
  Order pull() {
    try {
      return queue.remove();
    } catch (NoSuchElementException e) {
      return null;
    }
  }


  private static Comparator<Order> newDecayComparator(Clock clock) {
    return (a, b) -> {
      val shelfLifeA = DecayUtil.getRemainingShelfLife(clock, a);
      val shelfLifeB = DecayUtil.getRemainingShelfLife(clock, b);
      return Long.compare(shelfLifeA, shelfLifeB);
    };
  }


  // It's worth defining our own checked exception so we can handle this explicit case.
  static class QueueOverFlowException extends Exception {
    private QueueOverFlowException(int capacity) {
      super("Queue is at the maximum capacity of: " + capacity);
    }
  }
}
