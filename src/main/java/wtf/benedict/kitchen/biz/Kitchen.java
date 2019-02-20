package wtf.benedict.kitchen.biz;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.biz.StorageAggregator.StorageState;

// TODO Cancel drivers.
// TODO If an order expires, the shelf doesn't pull from overflow.
public class Kitchen {
  private final DriverDepot driverDepot;
  private final StorageAggregator storageAggregator;
  private final StorageFactory storageFactory;

  private final List<Order> trashedOrders = new ArrayList<>();

  private Storage storage;


  public Kitchen(DriverDepot driverDepot, StorageAggregator storageAggregator, StorageFactory storageFactory) {
    this.driverDepot = driverDepot;
    this.storageAggregator = storageAggregator;
    this.storageFactory = storageFactory;
    reset();
  }


  public StorageState getState() {
    val pickups = driverDepot.getState();
    return storageAggregator.getState(storage, pickups, trashedOrders);
  }


  public void receiveOrder(Order order) {
    try {
      storage.put(order);
    } catch (StaleOrderException e) {
      trashedOrders.add(order);
    }

    val pickupTask = newPickupTask(this, order.getId());
    driverDepot.schedulePickup(pickupTask, order);
  }


  public void reset() {
    driverDepot.reset();
    trashedOrders.clear();
    storage = storageFactory.newStorage(newExpirationListener(), trashedOrders);
  }


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
      // TODO Cancel driver.
      trashedOrders.add(order);
    };
  }
}
