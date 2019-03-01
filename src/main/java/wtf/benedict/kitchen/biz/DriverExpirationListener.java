package wtf.benedict.kitchen.biz;

import lombok.AllArgsConstructor;
import net.jodah.expiringmap.ExpirationListener;

@AllArgsConstructor
public class DriverExpirationListener implements ExpirationListener<Long, Order> {
  private final DriverDepot driverDepot;
  private final Trash trash;


  @Override
  public void expired(Long id, Order order) {
    driverDepot.cancelPickup(order.getId());
    trash.add(order);
  }
}
