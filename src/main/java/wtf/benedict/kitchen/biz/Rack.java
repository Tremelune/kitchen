//package wtf.benedict.kitchen.biz;
//
//import java.time.Clock;
//
//import lombok.val;
//import wtf.benedict.kitchen.biz.Shelf.OverflowException;
//
//// TODO Cumulative decay rate over time...Time spent in overflow plus time spent in other queues.
//// TODO Pull from overflow on removal from shelf.
//// TODO Cancel drivers.
//// TODO Move overflow stuff when stuff is evicted from temp shelves.
//// TODO Test.
//// TODO Evict more efficiently. By shelf life.
//public class Rack {
//  private static final int TEMPERATURE_SHELF_CAPACITY = 15;
//  private static final int OVERFLOW_CAPACITY = 20;
//
//  // Priority queue capacity is the total capacity of all shelves.
//  private static final int PRIORITY_QUEUE_CAPACITY =
//      TEMPERATURE_SHELF_CAPACITY * 3 + OVERFLOW_CAPACITY;
//
//  private static final int TEMPERATURE_DECAY_MULTIPLIER = 1;
//  private static final int OVERFLOW_DECAY_MULTIPLIER = TEMPERATURE_DECAY_MULTIPLIER * 2;
//
//  private final Shelf hotShelf;
//  private final Shelf coldShelf;
//  private final Shelf frozenShelf;
//  private final Shelf overflowShelf;
//  private final ShelfLifePriorityQueue shelfLifeQueue;
//
//
//  public Rack(Clock clock) {
//    hotShelf = new Shelf(clock, TEMPERATURE_SHELF_CAPACITY, TEMPERATURE_DECAY_MULTIPLIER);
//    coldShelf = new Shelf(clock, TEMPERATURE_SHELF_CAPACITY, TEMPERATURE_DECAY_MULTIPLIER);
//    frozenShelf = new Shelf(clock, TEMPERATURE_SHELF_CAPACITY, TEMPERATURE_DECAY_MULTIPLIER);
//    overflowShelf = new Shelf(clock, OVERFLOW_CAPACITY, OVERFLOW_DECAY_MULTIPLIER);
//    shelfLifeQueue = new ShelfLifePriorityQueue(clock, PRIORITY_QUEUE_CAPACITY);
//  }
//
//
//  public void receiveOrder(Order order) {
//    try {
//      val shelf = getShelf(order);
//      shelf.put(order);
//    } catch (OverflowException e) {
//      handleOverflow(order);
//    }
//
//    // TODO Dispatch driver.
//  }
//
//
//  // Try and add to the overflow shelf. If it's full, pull an order off the overflow queue and try
//  // again. We have to trash an order, so we might as well trash the one that will expire soonest
//  // anyway.
//  //
//  // We synchronize to be sure adds/removes happen in this exact order, even if another order is
//  // being received simultaneously.
//  private synchronized void handleOverflow(Order order) {
//    try {
//      // First try and stash the order on the overflow shelf.
//      overflowShelf.put(order);
//      shelfLifeQueue.add(order, OVERFLOW_DECAY_MULTIPLIER);
//    } catch (OverflowException e) {
//      try {
//        // The overflow shelf is full...so make some room and then add it.
//        val orderToiscard = shelfLifeQueue.pull();
//        overflowShelf.remove(orderToiscard.getId());
//        overflowShelf.put(order);
//      } catch (OverflowException e1) {
//        throw new RuntimeException("Could not add order: " + order, e1);
//      }
//    }
//  }
//
//
//  private Shelf getShelf(Order order) {
//    switch(order.getTemp()){
//      case HOT:
//        return hotShelf;
//      case COLD:
//        return coldShelf;
//      case FROZEN:
//        return frozenShelf;
//      default:
//        throw new IllegalArgumentException("No shelf found for temp: " + order.getTemp());
//    }
//  }
//
//
//  private void add(Order order) {
//
//  }
//}
