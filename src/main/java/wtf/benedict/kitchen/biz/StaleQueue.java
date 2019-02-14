package wtf.benedict.kitchen.biz;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.time.Clock;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.collections4.map.PassiveExpiringMap.ExpirationPolicy;

import lombok.AllArgsConstructor;
import lombok.val;

// TODO Instant notification of eviction.
// TODO Pull by ID.
// TODO Test.
class StaleQueue {
  private final PassiveExpiringMap<Long, Order> freshOrders;
  private final Map<Long, DecoratedOrder> sortedOrdersMap;
  private final SortedSet<DecoratedOrder> sortedOrders;

  private final int capacity;


  StaleQueue(Clock clock, int capacity, double decayRateMultiplier) {
    this.capacity = capacity;

    val expirationPolicy = newExpirationPolicy(clock, decayRateMultiplier);
    freshOrders = new PassiveExpiringMap<>(expirationPolicy);

    sortedOrders = new TreeSet<>(newDecayComparator(clock));
    sortedOrdersMap = new HashMap<>();
  }


  void put(Order order, double decayRateMultiplier) throws OverflowException {
    if (freshOrders.size() >= capacity) {
      throw new OverflowException(capacity);
    }

    val decoratedOrder = new DecoratedOrder(order, decayRateMultiplier);
    freshOrders.put(order.getId(), order);
    sortedOrders.add(decoratedOrder);
    sortedOrdersMap.put(order.getId(), decoratedOrder);
  }


  // Pulls stalest order.
  Order pull() {
    return get(true);
  }

  // Gets stalest order without removing it.
  Order peek() {
    return get(false);
  }


  // Gets the stalest order.
  private Order get(boolean isPull) {
    while (isNotEmpty(sortedOrders)) {
      // If this order isn't in idToOrder, it was likely evicted, so keep getting the next stinkiest
      // until we find something or the set is exhausted.
      val stinkiestOrder = sortedOrders.last();
      val order = freshOrders.get(stinkiestOrder.order.getId());
      if (order != null) {
        // We found the stinkiest order, so remove it to complete the "pull"...unless we're peeking.
        if (isPull) {
          freshOrders.remove(order.getId());
          sortedOrders.remove(stinkiestOrder);
          sortedOrdersMap.remove(order.getId());
        }
        return order;
      }

      // This order was evicted, so remove it from the set and tell the world.
      sortedOrders.remove(stinkiestOrder);
      sendEvictionNotification(stinkiestOrder.order.getId());
    }

    // No orders...We out.
    return null;
  }


  private void sendEvictionNotification(long orderId) {
    // TODO bruh
  }


  private static ExpirationPolicy<Long, Order> newExpirationPolicy(
      Clock clock, double decayRateMultiplier) {

    // Return must be in the future. PassiveExpiringMap uses system time, an thus so must we here...
    return (ExpirationPolicy<Long, Order>) (id, order) -> {
      val shelfLife = DecayUtil.getRemainingShelfLife(clock, order, decayRateMultiplier);
      val shelfLifeMillis = shelfLife * 1000; // Convert to millis
      return System.currentTimeMillis() + shelfLifeMillis;
    };
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
    // TODO Do we need this?
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


  // It's worth defining our own checked exception so we can handle this explicit case.
  static class OverflowException extends Exception {
    private OverflowException(int capacity) {
      super("At the maximum capacity of: " + capacity);
    }
  }
}
