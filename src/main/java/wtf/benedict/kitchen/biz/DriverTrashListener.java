package wtf.benedict.kitchen.biz;

import lombok.AllArgsConstructor;
import wtf.benedict.kitchen.biz.Trash.TrashListener;

@AllArgsConstructor
public class DriverTrashListener implements TrashListener {
  private final DriverDepot driverDepot;


  @Override
  public void onAdd(Order order) {
    driverDepot.cancelPickup(order.getId());
  }
}
