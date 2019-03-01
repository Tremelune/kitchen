package wtf.benedict.kitchen.biz.kitchen;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.kitchen.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.data.Temperature;

/** Ensures that the overflow shelf is eagerly pulled from when space frees up. */
@AllArgsConstructor
public class OverflowBalancer {
  private final OverflowShelf overflowShelf;
  private final Trash trash;


  /** Pulls orders from overflow and adds them to the temperature shelf, if possible. */
  void balance(Shelf shelf, Temperature temp) {
    val overflowOrder = overflowShelf.pullStalest(temp);
    if (overflowOrder != null) {
      try {
        shelf.put(overflowOrder);
      } catch (StaleOrderException e) {
        // If this happens, it means another order was added to overflow between the pull and put
        // above, and the order we just pulled from overflow is now the stalest...which means it
        // should be discarded.
        trash.add(overflowOrder);
      }
    }
  }
}
