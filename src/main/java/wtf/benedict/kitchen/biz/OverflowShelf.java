package wtf.benedict.kitchen.biz;

import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.OrderQueue.OverflowException;

/**
 * Holds orders that the temperature shelves don't have space for.
 *
 * Decay rate is accelerated, and overflow from here winds up in the trash.
 */
@AllArgsConstructor
public class OverflowShelf {
  private final ShelfStorage shelfStorage;
  private final Trash trash;
  private final int capacity;


  /**
   * Puts an order on the shelf. If the shelf is at-capacity, one of two things occur:
   *
   * 1) The stalest order is trashed and the incoming order is added.
   * 2) The incoming order is the stalest, and it is rejected.
   */
  synchronized void put(Order order) throws StaleOrderException {
    try {
      if (shelfStorage.countAll() < capacity) {
        shelfStorage.put(order);
      } else {
        val stalestOrder = getStalest(order);
        if (stalestOrder.getId() == order.getId()) {
          throw new StaleOrderException(order);
        }

        // Make space by trashing the stalest order.
        val stalestOverflow = pullStalest(stalestOrder.getTemp());
        trash.add(stalestOverflow);

        // Add this new freshness.
        shelfStorage.put(order);
      }
    } catch (OverflowException e) {
      throw new IllegalStateException("Overflow shelf is full for: " + order);
    }
  }


  /** Pulls the order with the lowest remaining shelf life by temperature. */
  synchronized Order pullStalest(Temperature temp) {
    return shelfStorage.pullStalest(temp);
  }


  /** Pulls the order by ID. */
  synchronized Order pull(long orderId) {
    return shelfStorage.pull(orderId);
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
    val stalest = shelfStorage.getStalest(temp);
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
