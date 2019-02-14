package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.AllArgsConstructor;
import lombok.val;

/**
 * A queue in order of remaining shelf life. Orders that are nearest to expiration get pulled first.
 */
class ShelfLifePriorityQueue {
  private final Clock clock;
  private final int capacity;
  private final SortedSet<DecoratedOrder> orders;


  ShelfLifePriorityQueue(Clock clock, int capacity) {
    this.clock = clock;
    this.capacity = capacity;
    orders = new TreeSet<>(newDecayComparator(clock));
  }


  // We synchronize in case something is added between the checks, remove, and add.
  synchronized void add(Order order, double decayRateMultiplier) {
    if (orders.size() >= capacity) {
      val remainingShelfLife = DecayUtil.getRemainingShelfLife(clock, order, decayRateMultiplier);
      val first = orders.first();

      val highestRemainingShelfLife =
          DecayUtil.getRemainingShelfLife(clock, first.order, decayRateMultiplier);

      // If the order we're trying to add has the highest remaining shelf life, we don't need it in
      // the queue. Lowest remaining shelf life is the priority. Just drop it.
      if (remainingShelfLife >= highestRemainingShelfLife) {
        return;
      }

      orders.remove(first);
    }

    val decoratedOrder = new DecoratedOrder(order, decayRateMultiplier);
    orders.add(decoratedOrder);
  }


  /** If the queue is empty, this will return null. I just seems more practical. */
  Order pull() {
    try {
      val decoratedOrder = orders.last();
      orders.remove(decoratedOrder);
      return decoratedOrder.order;
    } catch (NoSuchElementException e) {
      return null;
    }
  }


  private static Comparator<DecoratedOrder> newDecayComparator(Clock clock) {
    return (a, b) -> {
      val shelfLifeA = DecayUtil.getRemainingShelfLife(clock, a.order, a.decayRateMultiplier);
      val shelfLifeB = DecayUtil.getRemainingShelfLife(clock, b.order, b.decayRateMultiplier);
      return Long.compare(shelfLifeA, shelfLifeB);
    };
  }


  @AllArgsConstructor
  private static class DecoratedOrder {
    private final Order order;
    private final double decayRateMultiplier;

    // Somewhat generated code. As Orders are entities, we can rely on their IDs for uniqueness.
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DecoratedOrder that = (DecoratedOrder) o;
      return order.getId() == that.order.getId();
    }

    @Override
    public int hashCode() {
      return Objects.hash(order);
    }
  }
}
