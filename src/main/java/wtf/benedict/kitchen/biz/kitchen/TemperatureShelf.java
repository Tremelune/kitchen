package wtf.benedict.kitchen.biz.kitchen;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.kitchen.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.data.storage.CapacityExceededException;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.storage.ShelfStorage;

/** Stores orders of a particular temperature. */
@AllArgsConstructor
public class TemperatureShelf {
  private final double overflowDecayRate;
  private final OverflowBalancer overflowBalancer;
  private final OverflowShelf overflowShelf;
  private final ShelfStorage shelfStorage;


  /**
   * Adds order to the shelf. If the shelf is full, the freshest order is moved to the overflow
   * shelf.
   *
   * @throws StaleOrderException if the order being added is staler than the stalest order on the
   * overflow shelf.
   */
  void put(Order order) throws StaleOrderException {
    try {
      shelfStorage.put(order);
    } catch (CapacityExceededException e) {
      handleOverflow(order);
    }
  }


  /**
   * Pulls order by ID, removing it from the shelf. If there are orders of the matching temperature
   * on the overflow shelf, they are pulled in.
   */
  synchronized Order pull(long orderId) {
    Order order = shelfStorage.pull(orderId);
    if (order == null) {
      // No order here...but check overflow for it, but don't try and pull more from there...
      return overflowShelf.pull(orderId);
    }

    overflowBalancer.balance(this, order.getTemp());

    return order;
  }


  // This finds the freshest order (including the new one passed in) and sends it to overflow.
  synchronized private void handleOverflow(Order order)
      throws StaleOrderException {

    val currentFreshest = shelfStorage.getFreshest(order.getTemp());
    if (currentFreshest == null) {
      overflowShelf.put(order);
      return;
    }

    long currentFreshestLife = currentFreshest.calculateRemainingShelfLifeAt(overflowDecayRate);
    long orderLife = order.calculateRemainingShelfLifeAt(overflowDecayRate);

    if (orderLife > currentFreshestLife) {
      overflowShelf.put(order);
    } else {
      overflowShelf.put(currentFreshest);
      shelfStorage.pull(currentFreshest.getId());

      // This is recursive, but we've changed the state so that there will be room this time. If
      // another order sneaks in, it is correct to run through the overflow logic again.
      put(order);
    }
  }
}
