package wtf.benedict.kitchen.biz;

import java.time.Clock;

import lombok.val;
import wtf.benedict.kitchen.biz.Order.Temperature;
import wtf.benedict.kitchen.biz.OrderQueue.OverflowException;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.biz.StaleOrderSet.DecoratedOrder;

class TemperatureShelf {
  private static final int DECAY_RATE = 1;

  private final Clock clock;
  private final OrderQueue queue;
  private final OverflowShelf overflowShelf;
  private final Temperature temp;


  TemperatureShelf(Clock clock, int capacity, OverflowShelf overflowShelf, Temperature temp) {
    this.clock = clock;
    this.queue = new OrderQueue(clock, capacity, DECAY_RATE);
    this.overflowShelf = overflowShelf;
    this.temp = temp;
  }


  void put(Order order) throws StaleOrderException {
    if (order.getTemp() != temp){
      throw new IllegalArgumentException("Order temperature must be " + temp + ": " + order);
    }

    try {
      queue.put(order, 1); // TODO: Still sweatin' this weird value.
    } catch (OverflowException e) {
      handleOverflow(order);
    }
  }


  Order pull(long orderId) {
    Order order = queue.pull(orderId);
    if (order == null) {
      // No order here...but check overflow for it, but don't try and pull more from there...
      return overflowShelf.pull(temp, orderId);
    }

    // We have space now, so see if we can grab stuff from overflow. That stuff is decaying rapidly.
    val overflowOrder = overflowShelf.pullStalest(order.getTemp());
    if (overflowOrder != null) {
      try {
        put(overflowOrder);
      } catch (StaleOrderException e) {
        // If this happens, it means another order was added to overflow between the pull and put
        // above, and the order we just pulled from overflow is now the stalest...which means it
        // should be discarded. So we're done.
      }
    }

    return order;
  }


  private void handleOverflow(Order order) throws StaleOrderException {
    val currentFreshest = queue.peekFreshest();
    if (currentFreshest == null) {
      overflowShelf.put(order);
      return;
    }

    val orderLife = DecayUtil.getRemainingShelfLife(clock, order, 1);
    val currentFreshestLife = DecayUtil.getRemainingShelfLife(clock, currentFreshest, 1);
    if (orderLife > currentFreshestLife) {
      overflowShelf.put(order);
    } else {
      overflowShelf.put(currentFreshest);
      queue.pull(currentFreshest.getId());

      // This is recursive, but we've changed the state so that there will be room this time. If
      // another order sneaks in, it is correct to run through the overflow logic again.
      put(order);
    }
  }


  private Order getFreshest(Order a, Order b) {
    val orders = new StaleOrderSet(clock);
    orders.add(decorate(a));
    orders.add(decorate(b));
    return orders.last().getOrder();
  }

  private static DecoratedOrder decorate(Order order) {
    return new DecoratedOrder(order, DECAY_RATE);
  }
}
