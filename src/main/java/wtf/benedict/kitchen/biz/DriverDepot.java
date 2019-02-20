package wtf.benedict.kitchen.biz;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

/**
 * Handles the dispatch and cancellation of delivery drivers, while keeping track of their
 * expected time of pickup.
 */
public class DriverDepot {
  private final Map<Long, Pickup> orderIdToPickup = new HashMap<>();
  private final Random random = new Random();

  private final Clock clock;


  public DriverDepot(Clock clock) {
    this.clock = clock;
    reset();
  }


  /** "Sends" for a driver to come pickup up this order. */
  void schedulePickup(TimerTask pickupTask, Order order) {
    dispatchDriver(pickupTask, order);
  }

  /** Cancels driver. They just...disappear... */
  synchronized void cancelPickup(long orderId) {
    orderIdToPickup.remove(orderId);
  }


  /** @return State of "active" delivery drivers. */
  Map<Long, Pickup> getState() {
    return new HashMap<>(orderIdToPickup); // Hide the internal state from callers.
  }


  /** Clears current driver/pickup state in preparation for a fresh start. */
  void reset() {
    orderIdToPickup.clear();
  }

  private void dispatchDriver(TimerTask pickupTask, Order order) {
    // This ensures orders are removed from the map when picked up.
    val removalTask = new TimerTask() {
      @Override
      public void run() {
        pickupTask.run();
        orderIdToPickup.remove(order.getId());
      }
    };

    int pickupDelay = getPickupDelay();
    val timer = new Timer("Pickup Timer - " + order.getId());
    timer.schedule(removalTask, pickupDelay);

    val pickupTime = clock.instant().plus(pickupDelay, MILLIS);
    val delivery = new Pickup(order, pickupTime);

    orderIdToPickup.put(order.getId(), delivery);
  }


  // Returns a delay in millis, between 2000-10000. If you want to slow everything in the app down
  // to see what's really going on, increasing the delay and range here works well. This value is
  // specified in the challenge.
  private int getPickupDelay() {
    val delay = random.nextInt(8) + 2;
    return delay * 1000;
  }


  @AllArgsConstructor
  @Getter
  static class Pickup {
    private Order order;
    private Instant time;
  }
}
