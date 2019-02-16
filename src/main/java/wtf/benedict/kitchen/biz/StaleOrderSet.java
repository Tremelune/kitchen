package wtf.benedict.kitchen.biz;

import java.util.Comparator;
import java.util.TreeSet;

class StaleOrderSet extends TreeSet<Order> {
  StaleOrderSet() {
    super(newDecayComparator());
  }


  private static Comparator<Order> newDecayComparator() {
    // We need to include ID, or TreeSet will consider elements with the same remainingShelfLife
    // to be identical.
    return Comparator.comparingLong(Order::calculateRemainingShelfLife)
        .thenComparing(Order::getId);
  }
}
