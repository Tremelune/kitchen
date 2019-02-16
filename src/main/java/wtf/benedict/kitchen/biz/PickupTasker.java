package wtf.benedict.kitchen.biz;

import java.util.TimerTask;

import lombok.AllArgsConstructor;

// This class exists primarily to avoid a cyclical dependency between Kitchen and DeliveryDepot.
@AllArgsConstructor
class PickupTasker {
  private final Kitchen kitchen;


  TimerTask newTask(long orderId) {
    return new TimerTask() {
      public void run() {
        kitchen.pickupOrder(orderId);
      }
    };
  }
}
