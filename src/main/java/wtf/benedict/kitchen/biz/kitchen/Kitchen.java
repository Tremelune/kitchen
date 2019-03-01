package wtf.benedict.kitchen.biz.kitchen;

import java.util.TimerTask;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.delivery.DriverDepot;
import wtf.benedict.kitchen.biz.kitchen.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.data.Order;

/** Receives and stores orders while coordinating with delivery drivers. */
  @AllArgsConstructor
public class Kitchen {
  private final DriverDepot driverDepot;
  private final TemperatureShelf shelf;
  private final Trash trash;


  /** "Makes" an order, places it on the appropriate shelf, and sends for a driver to pick it up. */
  public void receiveOrder(Order order) {
    try {
      shelf.put(order);
    } catch (StaleOrderException e) {
      trash.add(order);
    }

    val pickupTask = newPickupTask(this, order.getId());
    driverDepot.schedulePickup(pickupTask, order);
  }


  /** Pulls order from shelves by ID. */
  Order pickupOrder(long orderId) {
    return shelf.pull(orderId);
  }


  private TimerTask newPickupTask(Kitchen kitchen, long orderId) {
    return new TimerTask() {
      public void run() {
        kitchen.pickupOrder(orderId);
      }
    };
  }
}
