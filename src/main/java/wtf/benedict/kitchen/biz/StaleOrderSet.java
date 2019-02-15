package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.util.Comparator;
import java.util.TreeSet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

class StaleOrderSet extends TreeSet<StaleOrderSet.DecoratedOrder> {
  StaleOrderSet(Clock clock) {
    super(newDecayComparator(clock));
  }


  private static Comparator<DecoratedOrder> newDecayComparator(Clock clock) {
    return (a, b) -> {
      val shelfLifeA = DecayUtil.getRemainingShelfLife(clock, a.order, a.decayRateMultiplier);
      val shelfLifeB = DecayUtil.getRemainingShelfLife(clock, b.order, b.decayRateMultiplier);
      return Long.compare(shelfLifeA, shelfLifeB);
    };
  }


  @AllArgsConstructor
  @Getter
  static class DecoratedOrder {
    private final Order order;
    private final double decayRateMultiplier;
  }
}
