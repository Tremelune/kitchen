package wtf.benedict.kitchen.biz;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.OrderQueue.OverflowException;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;

class TemperatureShelf {
  private static final int DECAY_RATE = 1;

  final OrderQueue queue;
  private final OverflowShelf overflowShelf;
  private final Temperature temp;
  private final Trash trash;


  TemperatureShelf(int capacity, OverflowShelf overflowShelf, Temperature temp, ExpirationListener<Long, Order> expirationListener, Trash trash) {
    val listener = wrapListener(expirationListener);
    this.queue = new OrderQueue(capacity, DECAY_RATE, listener);
    this.overflowShelf = overflowShelf;
    this.temp = temp;
    this.trash = trash;
  }


  void put(Order order) throws StaleOrderException {
    if (order.getTemp() != temp){
      throw new IllegalArgumentException("Order temperature must be " + temp + ": " + order);
    }

    try {
      queue.put(order);
    } catch (OverflowException e) {
      handleOverflow(order);
    }
  }


  synchronized Order pull(long orderId) {
    Order order = queue.pull(orderId);
    if (order == null) {
      // No order here...but check overflow for it, but don't try and pull more from there...
      return overflowShelf.pull(temp, orderId);
    }

    balanceFromOverflow(order.getTemp());

    return order;
  }


  // We have space now, so see if we can grab stuff from overflow. That stuff is decaying rapidly.
  private void balanceFromOverflow(Temperature temp) {
    val overflowOrder = overflowShelf.pullStalest(temp);
    if (overflowOrder != null) {
      try {
        put(overflowOrder);
      } catch (StaleOrderException e) {
        // If this happens, it means another order was added to overflow between the pull and put
        // above, and the order we just pulled from overflow is now the stalest...which means it
        // should be discarded.
        trash.add(overflowOrder);
      }
    }
  }


  synchronized private void handleOverflow(Order order) throws StaleOrderException {
    val currentFreshest = queue.peekFreshest();
    if (currentFreshest == null) {
      overflowShelf.put(order);
      return;
    }

    long currentFreshestLife =
        currentFreshest.calculateRemainingShelfLifeAt(OverflowShelf.DECAY_RATE);

    long orderLife =
        order.calculateRemainingShelfLifeAt(OverflowShelf.DECAY_RATE);

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


  // This makes sure we grab from overflow when space frees up due to passive expiration...This
  // will only actually happen if we increase the pickup delay from the specs of the challenge.
  private ExpirationListener<Long, Order> wrapListener(ExpirationListener<Long, Order> listener) {
    return (id, order) -> {
      listener.expired(id, order);
      balanceFromOverflow(order.getTemp());
    };
  }
}
