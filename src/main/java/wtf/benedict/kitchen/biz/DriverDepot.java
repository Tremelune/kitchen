package wtf.benedict.kitchen.biz;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Clock;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.DriverStorage.Pickup;

/**
 * Handles the dispatch and cancellation of delivery drivers, while keeping track of their
 * expected time of pickup.
 */
@AllArgsConstructor
public class DriverDepot {
  private final Clock clock;
  private final DriverStorage driverStorage;

  private final Random random = new Random();


  /** "Sends" for a driver to come pickup up this order. */
  void schedulePickup(TimerTask pickupTask, Order order) {
    dispatchDriver(pickupTask, order);
  }

  /** Cancels driver. They just...disappear... */
  synchronized void cancelPickup(long orderId) {
    driverStorage.delete(orderId);
  }


  private void dispatchDriver(TimerTask pickupTask, Order order) {
    // This ensures orders are removed from the map when picked up.
    val removalTask = new TimerTask() {
      @Override
      public void run() {
        pickupTask.run();
        driverStorage.delete(order.getId());
      }
    };

    int pickupDelay = getPickupDelay();
    val timer = new Timer("Pickup Timer - " + order.getId());
    timer.schedule(removalTask, pickupDelay);

    val pickupTime = clock.instant().plus(pickupDelay, MILLIS);
    val pickup = new Pickup(order, pickupTime);
    driverStorage.add(pickup);
  }


  // Returns a delay in millis, between 2000-10000. If you want to slow everything in the app down
  // to see what's really going on, increasing the delay and range here works well. This value is
  // specified in the challenge.
  private int getPickupDelay() {
    val delay = random.nextInt(8) + 2;
    return delay * 1000;
  }
}
