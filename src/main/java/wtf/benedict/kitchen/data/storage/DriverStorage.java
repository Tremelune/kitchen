package wtf.benedict.kitchen.data.storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wtf.benedict.kitchen.data.Order;

/** Current state of delivery drivers and their time-to-pickup. */
public class DriverStorage {
  private final Map<Long, Pickup> orderIdToPickup = new HashMap<>();


  public void add(Pickup pickup) {
    orderIdToPickup.put(pickup.order.getId(), pickup);
  }


  /** Cancels driver. They just...disappear... */
  public void delete(long orderId) {
    orderIdToPickup.remove(orderId);
  }


  /** @return State of "active" delivery drivers. */
  public List<Pickup> getAll() {
    return new ArrayList<>(orderIdToPickup.values()); // Hide the internal state from callers.
  }


  /** Clears current driver/pickup state in preparation for a fresh start. */
  public void deleteAll() {
    orderIdToPickup.clear();
  }


  @AllArgsConstructor
  @Getter
  public static class Pickup {
    private Order order;
    private Instant time;
  }
}
