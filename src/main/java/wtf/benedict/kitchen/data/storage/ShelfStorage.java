package wtf.benedict.kitchen.data.storage;

import static wtf.benedict.kitchen.data.Temperature.COLD;
import static wtf.benedict.kitchen.data.Temperature.FROZEN;
import static wtf.benedict.kitchen.data.Temperature.HOT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.Temperature;

/** State for the storage shelves. */
public class ShelfStorage {
  private final Map<Temperature, OrderQueue> queues = new HashMap<>();


  public ShelfStorage(
      int capacity, double decayRate, ExpirationListener<Long, Order> expirationListener) {

    queues.put(HOT, new OrderQueue(capacity, decayRate, expirationListener));
    queues.put(COLD, new OrderQueue(capacity, decayRate, expirationListener));
    queues.put(FROZEN, new OrderQueue(capacity, decayRate, expirationListener));
  }


  /** Pulls the order with the lowest remaining shelf life by temperature. */
  public Order pullStalest(Temperature temp) {
    return queues.get(temp).pullStalest();
  }


  /** Pulls the order by ID. */
  public Order pull(long orderId) {
    for (val temp : queues.keySet()) {
      val order = queues.get(temp).pull(orderId);
      if (order != null) {
        return order;
      }
    }

    return null;
  }


  /** Gets the order with the lowest remaining shelf life by temperature. */
  public Order getStalest(Temperature temp) {
    return queues.get(temp).peekStalest();
  }

  /** Gets the order with the highest remaining shelf life by temperature. */
  public Order getFreshest(Temperature temp) {
    return queues.get(temp).peekFreshest();
  }


  /** Gets all orders. */
  public List<Order> getAll() {
    val orders = new ArrayList<Order>();

    for (OrderQueue queue : queues.values()) {
      orders.addAll(queue.getAll());
    }

    return orders;
  }


  /** Gets all orders for a particular temperature shelf. */
  public List<Order> getAll(Temperature temp) {
    return new ArrayList<>(queues.get(temp).getAll());
  }


  /** Total number of orders being stored. */
  public int countAll() {
    int count = 0;
    for (val temp : queues.keySet()) {
      count += queues.get(temp).size();
    }
    return count;
  }


  public void put(Order order) throws CapacityExceededException {
    queues.get(order.getTemp()).put(order);
  }


  public void deleteAll() {
    for (val queue : queues.values()) {
      queue.deleteAll();
    }
  }
}
