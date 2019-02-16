package wtf.benedict.kitchen.api;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.Kitchen;
import wtf.benedict.kitchen.biz.Order;
import wtf.benedict.kitchen.biz.OrderMessage;
import wtf.benedict.kitchen.biz.Temperature;

// TODO Poisson distribution
@AllArgsConstructor
public class OrderGenerator {
  private final Clock clock;
  private final OrderLoader orderLoader;

  private final AtomicLong id = new AtomicLong(1); // Arbitrary, but zero is just an uncommon ID...


  void generateOrders(Kitchen kitchen) {
    val message = orderLoader.next();
    val order = asOrder(message);
    kitchen.receiveOrder(order);
  }


  private Order asOrder(OrderMessage message) {
    return new Order.Builder()
        .clock(clock)
        .id(nextOrderId())
        .name(message.getName())
        .temp(Temperature.fromValue(message.getTemp()))
        .baseDecayRate(message.getDecayRate())
        .initialShelfLife(message.getShelfLife())
        .build();
  }



  private long nextOrderId() {
    return id.getAndIncrement();
  }
}
