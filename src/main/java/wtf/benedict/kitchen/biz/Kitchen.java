package wtf.benedict.kitchen.biz;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.biz.StorageAggregator.StorageState;

// TODO Cancel drivers.
// TODO Include drivers in state.
// TODO Per-order decay strategy.
// TODO Display.
// TODO Enterprisize. Event sourcing...caching...message bus...CQRS...nine microservices...
public class Kitchen {
  private final StorageAggregator storageAggregator;
  private final StorageFactory storageFactory;

  private final Random random = new Random();

  private Storage storage;


  public Kitchen(StorageAggregator storageAggregator, StorageFactory storageFactory) {
    this.storageAggregator = storageAggregator;
    this.storageFactory = storageFactory;
    reset();
  }


  public StorageState getState() {
    return storageAggregator.getState(storage);
  }


  public void receiveOrder(Order order) {
    try {
      storage.put(order);
    } catch (StaleOrderException e) {
      val message = String.format("Refund customer! Order is too stale: %s", order);
      throw new UnsupportedOperationException(message);
    }

    dispatchDriver(order.getId());
  }


  public void reset() {
    storage = storageFactory.newStorage(newExpirationListener());
  }


  Order pickupOrder(long orderId) {
    return storage.pull(orderId);
  }


  void dispatchDriver(long orderId) {
    val task = newTask(this, orderId);
    val timer = new Timer("Pickup Timer - " + orderId);
    timer.schedule(task, getPickupDelay());
  }


  private TimerTask newTask(Kitchen kitchen, long orderId) {
    return new TimerTask() {
      public void run() {
        kitchen.pickupOrder(orderId);
      }
    };
  }


  // Returns a delay in millis, between 3-10s. Visible for testing.
  int getPickupDelay() {
    int secs = random.nextInt(8) + 2;
    return secs * 1000;
  }


  private ExpirationListener<Long, Order> newExpirationListener() {
    return (id, order) -> {
      // TODO
    };
  }
}
