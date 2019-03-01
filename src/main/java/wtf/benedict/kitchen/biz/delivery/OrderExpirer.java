package wtf.benedict.kitchen.biz.delivery;

import lombok.AllArgsConstructor;
import wtf.benedict.kitchen.biz.kitchen.Trash;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.storage.DriverStorage;

@AllArgsConstructor
public class OrderExpirer {
  private final DriverStorage driverStorage;
  private final Trash trash;


  public void expireOrder(Order order) {
    driverStorage.delete(order.getId());
    trash.add(order);
  }
}
