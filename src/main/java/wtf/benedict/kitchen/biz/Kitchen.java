package wtf.benedict.kitchen.biz;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;

// TODO Cancel drivers.
// TODO Move overflow stuff when stuff is evicted from temp shelves.
// TODO Per-order decay strategy.
// TODO Display.
// TODO Enterprisize. Event sourcing...caching...message bus...CQRS...nine microservices...
@AllArgsConstructor
public class Kitchen {
  private final Storage storage;

  private final Random random = new Random();


  public void receiveOrder(Order order) {
    try {
      storage.put(order);
    } catch (StaleOrderException e) {
      val message = String.format("Refund customer! Order is too stale: %s", order);
      throw new UnsupportedOperationException(message);
    }

    dispatchDriver(order.getId());
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
}
