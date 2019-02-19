package wtf.benedict.kitchen.biz;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

public class DriverDepot {
  private final Map<Long, Delivery> orderIdToDelivery = new HashMap<>();
  private final Random random = new Random();


  public DriverDepot() {
    reset();
  }


  void schedulePickup(TimerTask pickupTask, Order order) {
    dispatchDriver(pickupTask, order);
  }


  Map<Long, Delivery> getState() {
    return new HashMap<>(orderIdToDelivery); // Hide the internal state from callers.
  }


  void reset() {
    orderIdToDelivery.clear();
  }


  private void dispatchDriver(TimerTask pickupTask, Order order) {
    // This ensures orders are removed from the map when picked up.
    val removalTask = new TimerTask() {
      @Override
      public void run() {
        pickupTask.run();
        orderIdToDelivery.remove(order.getId());
      }
    };

    int pickupDelay = getPickupDelay();
    val timer = new Timer("Pickup Timer - " + order.getId());
    timer.schedule(removalTask, pickupDelay);
    val delivery = new Delivery(order.getName(), Math.round(pickupDelay / 1000));
    orderIdToDelivery.put(order.getId(), delivery);
  }


  // Returns a delay in millis, between 2-10s. Visible for testing.
  int getPickupDelay() {
    int secs = random.nextInt(8) + 2;
    return secs * 1000;
  }


  /** This is part of the public API. Changes here might break it! */
  @AllArgsConstructor
  @Getter
  public static class Delivery {
    private String name;
    private long secondsUntilPickup;
  }
}
