package wtf.benedict.kitchen.biz;

import java.time.Clock;

import lombok.val;
import wtf.benedict.kitchen.biz.TrashPriorityQueue.QueueOverFlowException;

// TODO Cumulative decay rate over time...Time spent in overflow plus time spent in other queues.
// TODO Should probably have a "global" queue in order of shelf life, taking into account the above.
// TODO It seems like a driver should be assigned a specific order as opposed to the "latest", which
// TODO means each order is an entity.
// TODO Test.
public class Kitchen {
  private final TrashPriorityQueue hotShelf;
  private final TrashPriorityQueue coldShelf;
  private final TrashPriorityQueue frozenShelf;
  private final TrashPriorityQueue overflowShelf; // TODO Handle doubled decay rate.


  public Kitchen(Clock clock) {
    hotShelf = new TrashPriorityQueue(clock, 15);
    coldShelf = new TrashPriorityQueue(clock, 15);
    frozenShelf = new TrashPriorityQueue(clock, 15);
    overflowShelf = new TrashPriorityQueue(clock, 20);
  }


  public void receiveOrder(Order order) {
    try {
      val shelf = getShelf(order);
      shelf.add(order);
    } catch (QueueOverFlowException e) {
      handleOverflow(order);
    }

    // TODO Dispatch driver.
  }


  // Try and add to the overflow shelf. If it's full, pull an order off the overflow queue and try
  // again. We have to trash an order, so we might as well trash the one that will expire soonest
  // anyway.
  //
  // We synchronize to be sure adds/removes happen in this exact order, even if another order is
  // being received simultaneously.
  private synchronized void handleOverflow(Order order) {
    try {
      overflowShelf.add(order);
    } catch (QueueOverFlowException e) {
      try {
        overflowShelf.pull();
        overflowShelf.add(order);
      } catch (QueueOverFlowException e1) {
        throw new RuntimeException("Could not add order: " + order, e1);
      }
    }
  }


  private TrashPriorityQueue getShelf(Order order) {
    switch(order.getTemp()){
      case HOT:
        return hotShelf;
      case COLD:
        return coldShelf;
      case FROZEN:
        return frozenShelf;
      default:
        throw new IllegalArgumentException("No shelf found for temp: " + order.getTemp());
    }
  }
}
