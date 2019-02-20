package wtf.benedict.kitchen.biz;

import java.util.TimerTask;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.biz.StorageAggregator.StorageState;

// TODO Cancel drivers.
// TODO There's a concurrency error somewhere.
public class Kitchen {
  private final DriverDepot driverDepot;
  private final StorageAggregator storageAggregator;
  private final StorageFactory storageFactory;

  private Storage storage;


  public Kitchen(DriverDepot driverDepot, StorageAggregator storageAggregator, StorageFactory storageFactory) {
    this.driverDepot = driverDepot;
    this.storageAggregator = storageAggregator;
    this.storageFactory = storageFactory;
    reset();
  }


  public StorageState getState() {
    val pickups = driverDepot.getState();
    return storageAggregator.getState(storage, pickups);
  }


  public void receiveOrder(Order order) {
    try {
      storage.put(order);
    } catch (StaleOrderException e) {
      val message = String.format("Refund customer! Order is too stale: %s", order);
      throw new IllegalArgumentException(message);
    }

    val pickupTask = newPickupTask(this, order.getId());
    driverDepot.schedulePickup(pickupTask, order);
  }


  public void reset() {
    driverDepot.reset();
    storage = storageFactory.newStorage(newExpirationListener());
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
      // TODO
    };
  }
}
