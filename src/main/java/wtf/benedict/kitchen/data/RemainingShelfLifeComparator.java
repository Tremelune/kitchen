package wtf.benedict.kitchen.data;

import java.util.Comparator;

/** Sort by remaining shelf life. */
public class RemainingShelfLifeComparator {
  public static final Comparator<Order> INSTANCE = newRemainingShelfLifeComparator();


  // Note: If this is used in a SortedSet, it will treat elements with the same remaining shelf life
  // as the same, and one will disappear unless you also include an ID comparison. Ask me how I know...
  private static Comparator<Order> newRemainingShelfLifeComparator() {
    return Comparator.comparingLong(Order::calculateRemainingShelfLife);
  }
}
