package wtf.benedict.kitchen.biz;

import java.util.Random;
import java.util.Timer;

import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
class DeliveryDepot {
  private final Random random = new Random();

  private final PickupTasker pickupTasker;


  void dispatchDriver(long orderId) {
    val task = pickupTasker.newTask(orderId);
    val timer = new Timer("Pickup Timer - " + orderId);
    timer.schedule(task, getDelay());
  }


  // Returns a delay in millis, between 3-10s. Visible for testing.
  int getDelay() {
    int secs = random.nextInt(8) + 2;
    return secs * 1000;
  }
}
