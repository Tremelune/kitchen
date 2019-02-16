package wtf.benedict.kitchen.biz;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.collections4.map.PassiveExpiringMap.ExpirationPolicy;

import lombok.val;

// TODO Notification of eviction.
// TODO Calculate shelf life with proposed rate change in mind.
class OrderQueue {
  private final PassiveExpiringMap<Long, Order> freshOrders;

  private final int capacity;
  private final double decayRateMultiplier;


  OrderQueue(int capacity, double decayRateMultiplier) {
    if (capacity < 1) {
      throw new IllegalArgumentException("Capacity must be positive!");
    }

    this.capacity = capacity;
    this.decayRateMultiplier = decayRateMultiplier;
    freshOrders = new PassiveExpiringMap<>(newExpirationPolicy());
  }


  void put(Order order) throws OverflowException {
    if (freshOrders.size() >= capacity) {
      throw new OverflowException(capacity);
    }

    freshOrders.put(order.getId(), order);
    order.changeDecayRate(decayRateMultiplier);
  }


  Order pullStalest() {
    return get(true, true);
  }

  // Gets stalest order without removing it.
  Order peekStalest() {
    return get(false, true);
  }

  // Gets freshest order without removing it.
  Order peekFreshest() {
    return get(false, false);
  }


  Order pull(long orderId) {
    val order = freshOrders.get(orderId);
    freshOrders.remove(orderId);
    return order;
  }


  // Gets the stalest order. Synchronized to ensure nothing is removed mid-loop by another process.
  private synchronized Order get(boolean isPull, boolean findStalest) {
    if (isEmpty(freshOrders.keySet())) {
      return null;
    }

    val order = getMost(findStalest);
    if (isPull) {
      freshOrders.remove(order.getId());
    }
    return order;
  }


  // Get stalest or freshest. If freshOrders is empty, this will explode.
  private Order getMost(boolean stale) {
    val comparator = stale
        ? RemainingShelfLifeComparator.INSTANCE
        : RemainingShelfLifeComparator.INSTANCE.reversed();

    val orders = new ArrayList<Order>(freshOrders.values());
    orders.sort(comparator);

    return orders.iterator().next();
  }


  private static ExpirationPolicy<Long, Order> newExpirationPolicy() {
    return (ExpirationPolicy<Long, Order>) (id, order) -> {
      val shelfLifeMillis = order.calculateRemainingShelfLife() * 1000; // Convert to millis

      // PassiveExpiringMap uses system time, an thus so must we here...
      return System.currentTimeMillis() + shelfLifeMillis;
    };
  }


  // It's worth defining our own checked exception so we can handle this explicit case.
  static class OverflowException extends Exception {
    private OverflowException(int capacity) {
      super("At the maximum capacity of: " + capacity);
    }
  }
}