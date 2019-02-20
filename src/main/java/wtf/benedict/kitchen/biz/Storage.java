package wtf.benedict.kitchen.biz;

import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;

public class Storage {
  private static final int TEMPERATURE_SHELF_CAPACITY = 15;
  private static final int OVERFLOW_CAPACITY = 20;

  final OverflowShelf overflowShelf;
  final Map<Temperature, TemperatureShelf> tempToShelf;
  private final ExpirationListener<Long, Order> expirationListener;


  public Storage(ExpirationListener<Long, Order> expirationListener, Trash trash) {
    this.expirationListener = expirationListener;

    overflowShelf = new OverflowShelf(OVERFLOW_CAPACITY, expirationListener, trash);

    // This must happen after expirationListener and overflowSHelf are initialized.
    tempToShelf = ImmutableMap.of(
        HOT, newShelf(HOT, trash),
        COLD, newShelf(COLD, trash),
        FROZEN, newShelf(FROZEN, trash)
    );
  }


  void put(Order order) throws StaleOrderException {
    val shelf = tempToShelf.get(order.getTemp());
    shelf.put(order);
  }


  Order pull(long orderId) {
    // We could probably tell drivers the temperature of each order, but that's weird and there are
    // only three shelves, so let's just check each one until we find our order.
    for (val shelf : tempToShelf.values()) {
      val order = shelf.pull(orderId);
      if (order != null) {
        return order;
      }
    }

    return null;
  }


  private TemperatureShelf newShelf(Temperature temp, Trash trash) {
    return new TemperatureShelf(
        TEMPERATURE_SHELF_CAPACITY, overflowShelf, temp, expirationListener, trash);
  }
}
