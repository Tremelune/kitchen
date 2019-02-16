package wtf.benedict.kitchen.biz;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Comparator;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.collections4.map.PassiveExpiringMap.ExpirationPolicy;

import lombok.val;

// TODO Instant notification of eviction.
// TODO Calculate shelf life with proposed rate change in mind.
class OrderQueue {
  private final PassiveExpiringMap<Long, Order> freshOrders;
  private final StaleOrderSet sortedOrders = new StaleOrderSet();

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
    sortedOrders.add(order);
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
    removeOrder(orderId);
    return order;
  }


  // Gets the stalest order.
  private Order get(boolean isPull, boolean findStalest) {
    while (isNotEmpty(sortedOrders)) {
      // If this order isn't in idToOrder, it was likely evicted, so keep getting the next stalest
      // until we find something or the set is exhausted.
      val mostOrder = getMost(findStalest);
      val order = freshOrders.get(mostOrder.getId());
      if (order != null) {
        // We found the stalest order, so remove it to complete the "pull"...unless we're peeking.
        if (isPull) {
          removeOrder(order.getId());
        }
        return order;
      }

      // This order has been evicted, so remove it from the sorted set and tell the world.
      removeOrder(mostOrder.getId());
      sendEvictionNotification(mostOrder.getId());
    }

    // No orders...We out.
    return null;
  }


  // Get stalest or freshest
  private Order getMost(boolean stale) {
    return stale ? sortedOrders.first() : sortedOrders.last();
  }


  private void removeOrder(long orderId) {
    val orderToRemove = sortedOrders.stream()
        .filter((order) -> order.getId() == orderId)
        .findFirst()
        .orElse(null);

    freshOrders.remove(orderId);
    sortedOrders.remove(orderToRemove);
  }


  private void sendEvictionNotification(long orderId) {
    // TODO bruh
  }


  private static ExpirationPolicy<Long, Order> newExpirationPolicy() {
    return (ExpirationPolicy<Long, Order>) (id, order) -> {
      val shelfLifeMillis = order.calculateRemainingShelfLife() * 1000; // Convert to millis

      // PassiveExpiringMap uses system time, an thus so must we here...
      return System.currentTimeMillis() + shelfLifeMillis;
    };
  }

  private static Comparator<Order> newRemainingShelfLifeComparator() {
    return Comparator.comparingLong(Order::calculateRemainingShelfLife);
  }


  // It's worth defining our own checked exception so we can handle this explicit case.
  static class OverflowException extends Exception {
    private OverflowException(int capacity) {
      super("At the maximum capacity of: " + capacity);
    }
  }
}
