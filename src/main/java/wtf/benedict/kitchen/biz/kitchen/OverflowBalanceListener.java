package wtf.benedict.kitchen.biz.kitchen;

import lombok.AllArgsConstructor;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.data.Order;

/** This ensures we grab from overflow when space frees up due to passive expiration. */
// TODO Wait. How do I use this without cyclical dependencies?
@AllArgsConstructor
public class OverflowBalanceListener implements ExpirationListener<Long, Order> {
  private final ExpirationListener<Long, Order> expirationListener;
  private final OverflowBalancer overflowBalancer;
  private final TemperatureShelf temperatureShelf;


  @Override
  public void expired(Long orderId, Order order) {
    expirationListener.expired(orderId, order);
    overflowBalancer.balance(temperatureShelf, order.getTemp());
  }
}
