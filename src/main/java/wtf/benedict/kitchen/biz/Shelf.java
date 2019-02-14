package wtf.benedict.kitchen.biz;

import java.time.Clock;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.collections4.map.PassiveExpiringMap.ExpirationPolicy;

import lombok.val;

class Shelf {
  private final PassiveExpiringMap<Long, Order> idToOrder;

  private final int capacity;


  Shelf(Clock clock, int capacity, double decayRateMultiplier) {
    this.capacity = capacity;

    val expirationPolicy = newExpirationPolicy(clock, decayRateMultiplier);
    idToOrder = new PassiveExpiringMap<>(expirationPolicy);
  }


  // We synchronize in case something is added between the check and the add.
  synchronized void put(Order order) throws OverflowException {
    if (idToOrder.size() >= capacity) {
      throw new OverflowException(capacity);
    }

    idToOrder.put(order.getId(), order);
  }


  Order get(long id) {
    return idToOrder.get(id);
  }


  void remove(long id) {
    idToOrder.remove(id);
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
