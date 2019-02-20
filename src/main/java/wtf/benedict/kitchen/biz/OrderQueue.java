package wtf.benedict.kitchen.biz;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpiringMap;

class OrderQueue {
  final ExpiringMap<Long, Order> orders;

  private final int capacity;
  private final double decayRateMultiplier;


  OrderQueue(int capacity, double decayRateMultiplier, ExpirationListener<Long, Order> expirationListener) {
    if (capacity < 1) {
      throw new IllegalArgumentException("Capacity must be positive!");
    }

    this.capacity = capacity;
    this.decayRateMultiplier = decayRateMultiplier;
    orders = ExpiringMap.builder()
        .variableExpiration()
        .expirationListener(expirationListener)
        .build();
  }


  synchronized void put(Order order) throws OverflowException {
    if (orders.size() >= capacity) {
      throw new OverflowException(capacity);
    }

    order.changeDecayRate(decayRateMultiplier);
    orders.put(order.getId(), order, order.calculateRemainingShelfLife(), SECONDS);
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


  synchronized Order pull(long orderId) {
    val order = orders.get(orderId);
    orders.remove(orderId);
    return order;
  }


  // Gets the stalest order. Synchronized to ensure nothing is removed mid-loop by another process.
  private synchronized Order get(boolean isPull, boolean findStalest) {
    if (isEmpty(orders.keySet())) {
      return null;
    }

    val order = getMost(findStalest);
    if (isPull) {
      orders.remove(order.getId());
    }
    return order;
  }


  // Get stalest or freshest. If freshOrders is empty, this will explode.
  private Order getMost(boolean stale) {
    val comparator = stale
        ? RemainingShelfLifeComparator.INSTANCE
        : RemainingShelfLifeComparator.INSTANCE.reversed();

    val orders = new ArrayList<Order>(this.orders.values());
    orders.sort(comparator);

    return orders.iterator().next();
  }


  // It's worth defining our own checked exception so we can handle this explicit case.
  static class OverflowException extends Exception {
    private OverflowException(int capacity) {
      super("At the maximum capacity of: " + capacity);
    }
  }
}
