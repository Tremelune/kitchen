package wtf.benedict.kitchen.biz;

import java.util.TimerTask;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.biz.StorageAggregator.StorageState;
import wtf.benedict.kitchen.biz.Trash.TrashListener;

/** Receives and stores orders while coordinating with delivery drivers. */
public class Kitchen {
  private final DriverDepot driverDepot;
  private final StorageAggregator storageAggregator;
  private final StorageFactory storageFactory;

  private final Trash trash = new Trash(newTrashListener());

  private Storage storage;


  public Kitchen(DriverDepot driverDepot, StorageAggregator storageAggregator, StorageFactory storageFactory) {
    this.driverDepot = driverDepot;
    this.storageAggregator = storageAggregator;
    this.storageFactory = storageFactory;
    reset();
  }


  /**
   * @return State of the whole system, including waiting orders, shelves, trash, and delivery
   * drivers.
   */
  public StorageState getState() {
    val pickups = driverDepot.getState();
    return storageAggregator.getState(storage, pickups, trash);
  }


  /** "Makes" an order, places it on the appropriate shelf, and sends for a driver to pick it up. */
  public void receiveOrder(Order order) {
    try {
      storage.put(order);
    } catch (StaleOrderException e) {
      trash.add(order);
    }

    val pickupTask = newPickupTask(this, order.getId());
    driverDepot.schedulePickup(pickupTask, order);
  }


  /** Resets drivers, shelves, and trash. */
  public void reset() {
    driverDepot.reset();
    trash.reset();
    storage = storageFactory.newStorage(newExpirationListener(), trash);
  }


  /** Pulls order from shelves by ID. */
  Order pickupOrder(long orderId) {
    return storage.pull(orderId);
  }


  private TimerTask newPickupTask(Kitchen kitchen, long orderId) {
    return new TimerTask() {
      public void run() {
        kitchen.pickupOrder(orderId);
      }
    };
  }


  private ExpirationListener<Long, Order> newExpirationListener() {
    return (id, order) -> {
      driverDepot.cancelPickup(order.getId());
      trash.add(order);
    };
  }


  private TrashListener newTrashListener() {
    return order -> driverDepot.cancelPickup(order.getId());
  }
}
