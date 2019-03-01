package wtf.benedict.kitchen.biz.kitchen;

import lombok.AllArgsConstructor;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.storage.ShelfStorage;

/**
 * This exists to avoid a cyclical dependency between Shelf and ShelfStorage due to the overflow
 * balance listener (see Shelf.balanceFromOverFlow()).
 *
 * It just wouldn't be Java without a factory SOMEwhere...
 */
@AllArgsConstructor
public class ShelfStorageFactory {
  private final int capacity;
  private final double decayRate;


  ShelfStorage create(ExpirationListener<Long, Order> expirationListener) {
    return new ShelfStorage(capacity, decayRate, expirationListener);
  }
}
