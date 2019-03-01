package wtf.benedict.kitchen.api;

import java.time.Clock;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.CumulativeDecayStrategy;
import wtf.benedict.kitchen.biz.OrderMessage;
import wtf.benedict.kitchen.biz.kitchen.Kitchen;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.Temperature;

/**
 * Places orders to the kitchen to be made and stored for pickup.
 *
 * Orders are placed with a Poisson Distribution at an average of 3.25 orders per second. This can
 * be easily modified to change the rate of order placement.
 */
@AllArgsConstructor
public class OrderGenerator {
  private static final double ARRIVAL_AVERAGE = 3.25; // Specified in challange.

  private final Clock clock;
  private final OrderLoader orderLoader;

  private final AtomicLong id = new AtomicLong(1); // Arbitrary, but zero is just an uncommon ID...
  private final Random random = new Random();


  /** Pulls orders and places them according to our order placement strategy. */
  void generateOrders(Kitchen kitchen) {
    while (orderLoader.hasNext()) {
      val message = orderLoader.next();
      val order = asOrder(message);
      kitchen.receiveOrder(order);
      sleep();
    }
  }


  /** Clears already-loaded orders so they can be loaded fresh. */
  void reset() {
    orderLoader.reset();
  }


  private Order asOrder(OrderMessage message) {
    val decayStrategy = new CumulativeDecayStrategy(clock);

    return new Order.Builder()
        .id(nextOrderId())
        .name(message.getName())
        .temp(Temperature.fromValue(message.getTemp()))
        .baseDecayRate(message.getDecayRate())
        .initialShelfLife(message.getShelfLife())
        .decayStrategy(decayStrategy)
        .build();
  }



  private long nextOrderId() {
    return id.getAndIncrement();
  }


  private static void sleep() {
    try {
      Thread.sleep(poissonDelay());
    } catch (InterruptedException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  // https://stackoverflow.com/questions/2206199/how-do-i-generate-discrete-random-events-with-a-poisson-distribution/5615564#5615564
  private static long poissonDelay() {
    double delay = Math.log(1.0-Math.random())/-ARRIVAL_AVERAGE;
    return Math.round(delay * 1000); // Convert to seconds, round to long.
  }
}
