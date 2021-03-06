package wtf.benedict.kitchen.biz.kitchen;

import lombok.AllArgsConstructor;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.storage.DriverStorage;
import wtf.benedict.kitchen.data.storage.TrashStorage;

/**
 * Food that is pushed off all the shelves or isn't picked up before it expires is sent to the
 * trash. Happy birthday TO THE GROUND!
 */
@AllArgsConstructor
public class Trash {
  private final DriverStorage driverStorage;
  private final TrashStorage trashStorage;


  public void add(Order order) {
    trashStorage.add(order);
    driverStorage.delete(order.getId());
  }
}
