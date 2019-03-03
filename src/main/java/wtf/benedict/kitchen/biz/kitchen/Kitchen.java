package wtf.benedict.kitchen.biz.kitchen;

import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.delivery.DriverDepot;
import wtf.benedict.kitchen.biz.kitchen.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.data.Order;

/** Receives and stores orders while coordinating with delivery drivers. */
  @AllArgsConstructor
public class Kitchen {
  private final DriverDepot driverDepot;
  private final ExecutorService executorService;
  private final Shelf shelf;
  private final Trash trash;


  /** "Makes" an order, places it on the appropriate shelf, and sends for a driver to pick it up. */
  public void receiveOrder(Order order) {
    // Let's pretend putting orders on the shelves and scheduling drivers is slow, do the work
    // concurrently in separate threads, and return immediately so the order placer can move on.
    executorService.submit(() -> putOrder(order));
    executorService.submit(() -> scheduleDriver(order));
  }


  private void putOrder(Order order) {
    try {
      shelf.put(order);
    } catch (StaleOrderException e) {
      trash.add(order);
    }
  }


  private void scheduleDriver(Order order) {
    // Schedule the pickup task for the future, emulating driver delay
    val pickupTask = newPickupTask(this, order.getId());
    driverDepot.schedulePickup(pickupTask, order);
  }


  /** Removes order from shelves by ID. */
  private void pickupOrder(long orderId) {
    shelf.pull(orderId);
  }


  private TimerTask newPickupTask(Kitchen kitchen, long orderId) {
    return new TimerTask() {
      public void run() {
        kitchen.pickupOrder(orderId);
      }
    };
  }
}
