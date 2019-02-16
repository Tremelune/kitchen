package wtf.benedict.kitchen.biz;

import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.util.HashMap;
import java.util.Map;

import lombok.val;
import wtf.benedict.kitchen.biz.OrderQueue.OverflowException;

class OverflowShelf {
  private static final int DECAY_RATE = 2;

  private final Map<Temperature, OrderQueue> queues = new HashMap<>();

  private final int capacity;

  // The overflow shelf has a capacity that is spread across several order queues, so we keep track
  // of the true "size" of the shelf manually. This lets us fill up with orders of a single temp
  // while also limiting us to the specified capacity.
  private int size;


  OverflowShelf(int capacity) {
    this.capacity = capacity;

    queues.put(HOT, new OrderQueue(capacity, DECAY_RATE));
    queues.put(COLD, new OrderQueue(capacity, DECAY_RATE));
    queues.put(FROZEN, new OrderQueue(capacity, DECAY_RATE));
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
    // This set will order them, then just pull off the stalest.
    val orders = new StaleOrderSet();
    addStalest(orders, HOT);
    addStalest(orders, COLD);
    addStalest(orders, FROZEN);
    orders.add(order);
    return orders.first();
  }

  private void addStalest(StaleOrderSet orders, Temperature temp) {
    // Skip nulls.
    val stalest = queues.get(temp).peekStalest();
    if (stalest == null) {
      return;
    }

    orders.add(stalest);
  }


  static class StaleOrderException extends Exception {
    private StaleOrderException(Order order) {
      super("Order is stalest on the shelf: " + order);
    }
  }
}
