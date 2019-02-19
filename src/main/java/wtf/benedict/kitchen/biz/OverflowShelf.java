package wtf.benedict.kitchen.biz;

import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.OrderQueue.OverflowException;

class OverflowShelf {
  static final int DECAY_RATE = 2;

  final Map<Temperature, OrderQueue> queues = new HashMap<>();

  private final int capacity;

  // The overflow shelf has a capacity that is spread across several order queues, so we keep track
  // of the true "size" of the shelf manually. This lets us fill up with orders of a single temp
  // while also limiting us to the specified capacity.
  private int size;


  OverflowShelf(int capacity, ExpirationListener<Long, Order> expirationListener) {
    this.capacity = capacity;

    queues.put(HOT, new OrderQueue(capacity, DECAY_RATE, expirationListener));
    queues.put(COLD, new OrderQueue(capacity, DECAY_RATE, expirationListener));
    queues.put(FROZEN, new OrderQueue(capacity, DECAY_RATE, expirationListener));
  }


  void put(Order order) throws StaleOrderException {
    if (size < capacity) {
      enqueuOrder(order);
    } else {
      val stalestOrder = getStalest(order);
      if (stalestOrder.getId() == order.getId()) {
        throw new StaleOrderException(order);
      }

      // Make space and add the new order.
      pullStalest(stalestOrder.getTemp()); // Discard stalest order. Happy birthday TO THE GROUND!
      enqueuOrder(order);
    }
  }


  Order pullStalest(Temperature temp) {
    val order = queues.get(temp).pullStalest();
    size--;
    return order;
  }


  Order pull(Temperature temp, long orderId) {
    val order = queues.get(temp).pull(orderId);
    size--;
    return order;
  }


  private void enqueuOrder(Order order) {
    try {
      queues.get(order.getTemp()).put(order);
      size++;
    } catch (OverflowException e) {
      fatalOverflow(order);
    }
  }


  // This state is "impossible" because the shelf capacity is the same as the queue capacities, and
  // we check size before an order is ever added to a queue.
  private void fatalOverflow(Order order) {
    String message = "Could not add to overflow shelf of size %s capacity %s: %s";
    throw new IllegalStateException(String.format(message, size, capacity, order));
  }


  // Find the stalest order across all temps, including the newly-incoming order.
  private Order getStalest(Order order) {
    // Order them, then just pull off the stalest.
    val orders = new ArrayList<Order>();
    orders.add(order);
    addStalest(orders, HOT);
    addStalest(orders, COLD);
    addStalest(orders, FROZEN);

    orders.sort(RemainingShelfLifeComparator.INSTANCE);

    return orders.iterator().next();
  }

  private void addStalest(List<Order> orders, Temperature temp) {
    // Skip nulls.
    val stalest = queues.get(temp).peekStalest();
    if (stalest == null) {
      return;
    }

    orders.add(stalest);
  }


  static class StaleOrderException extends Exception {
    // Visible for testing.
    StaleOrderException(Order order) {
      super("Order is stalest on the shelf: " + order);
    }
  }
}
