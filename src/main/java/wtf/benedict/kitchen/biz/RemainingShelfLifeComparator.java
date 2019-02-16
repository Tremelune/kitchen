package wtf.benedict.kitchen.biz;

import java.util.Comparator;

class RemainingShelfLifeComparator {
  static final Comparator<Order> INSTANCE = newRemainingShelfLifeComparator();


  private static Comparator<Order> newRemainingShelfLifeComparator() {
    // We need to include ID, or sort will consider elements with the same remainingShelfLife to be
    // identical. TODO NOT ANYMORE
    return Comparator.comparingLong(Order::calculateRemainingShelfLife)
        .thenComparing(Order::getId);
  }
}
