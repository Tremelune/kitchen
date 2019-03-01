package wtf.benedict.kitchen.api;

import java.time.Clock;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.kitchen.Kitchen;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.biz.OrderMessage;
import wtf.benedict.kitchen.biz.CumulativeDecayStrategy;
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
      scheduleOrderArrival(kitchen, orderLoader.next());
    }
  }


  /** Clears already-loaded orders so they can be loaded fresh. */
  void reset() {
    orderLoader.reset();
  }


  private void scheduleOrderArrival(Kitchen kitchen, OrderMessage message) {
    val order = asOrder(message);
    val task = newOrderTask(kitchen, order);
    val timer = new Timer("Generate order");
    int delay = poissonDelay() * 1000; // Convert to seconds
    timer.schedule(task, delay);
  }


  private TimerTask newOrderTask(Kitchen kitchen, Order order) {
    return new TimerTask() {
      @Override
      public void run() {
        kitchen.receiveOrder(order);
      }
    };
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


  // https://stackoverflow.com/questions/9832919/generate-poisson-arrival-in-java
  private int poissonDelay() {
    int r = 0;
    double a = random.nextDouble();
    double p = Math.exp(-ARRIVAL_AVERAGE);

    while (a > p) {
      r++;
      a = a - p;
      p = p * ARRIVAL_AVERAGE / r;
    }
    return r;
  }
}
