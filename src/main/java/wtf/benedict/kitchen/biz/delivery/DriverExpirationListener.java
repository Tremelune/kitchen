package wtf.benedict.kitchen.biz.delivery;

import lombok.AllArgsConstructor;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.data.Order;

@AllArgsConstructor
public class DriverExpirationListener implements ExpirationListener<Long, Order> {
  private final OrderExpirer orderExpirer;


  @Override
  public void expired(Long id, Order order) {
    orderExpirer.expireOrder(order);
  }
}
