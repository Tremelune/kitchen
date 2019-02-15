package wtf.benedict.kitchen.biz;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.time.Clock;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.collections4.map.PassiveExpiringMap.ExpirationPolicy;

import lombok.val;
import wtf.benedict.kitchen.biz.StaleOrderSet.DecoratedOrder;

// TODO Instant notification of eviction.
class OrderQueue {
  private final PassiveExpiringMap<Long, Order> freshOrders;
  private final StaleOrderSet sortedOrders;

  private final int capacity;


  OrderQueue(Clock clock, int capacity, double decayRateMultiplier) {
    if (capacity < 1) {
      throw new IllegalArgumentException("Capacity must be positive!");
    }

    this.capacity = capacity;

    val expirationPolicy = newExpirationPolicy(clock, decayRateMultiplier);
    freshOrders = new PassiveExpiringMap<>(expirationPolicy);

    sortedOrders = new StaleOrderSet(clock);
  }


  void put(Order order, double decayRateMultiplier) throws OverflowException {
    if (freshOrders.size() >= capacity) {
      throw new OverflowException(capacity);
    }

    val decoratedOrder = new DecoratedOrder(order, decayRateMultiplier);
    freshOrders.put(order.getId(), order);
    sortedOrders.add(decoratedOrder);
  }


  Order pullStalest() {
    return get(true, true);
  }

  Order pullFreshest() {
    return get(true, false);
  }

  // Gets stalest order without removing it.
  Order peekStalest() {
    return get(false, true);
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
      val order = freshOrders.get(mostOrder.getOrder().getId());
      if (order != null) {
        // We found the stalest order, so remove it to complete the "pull"...unless we're peeking.
        if (isPull) {
          removeOrder(order.getId());
        }
        return order;
      }

      // This order has been evicted, so remove it from the sorted set and tell the world.
      removeOrder(mostOrder.getOrder().getId());
      sendEvictionNotification(mostOrder.getOrder().getId());
    }

    // No orders...We out.
    return null;
  }


  // Get stalest or freshest
  private DecoratedOrder getMost(boolean stale) {
    return stale ? sortedOrders.first() : sortedOrders.last();
  }


  private void removeOrder(long orderId) {
    val orderToRemove = sortedOrders.stream()
        .filter((order) -> order.getOrder().getId() == orderId)
        .findFirst()
        .orElse(null);

    freshOrders.remove(orderId);
    sortedOrders.remove(orderToRemove);
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


  // It's worth defining our own checked exception so we can handle this explicit case.
  static class OverflowException extends Exception {
    private OverflowException(int capacity) {
      super("At the maximum capacity of: " + capacity);
    }
  }
}
