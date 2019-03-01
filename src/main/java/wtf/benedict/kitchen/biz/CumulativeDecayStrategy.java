package wtf.benedict.kitchen.biz;

import static java.lang.Math.round;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.data.DecayStrategy;

/**
 * This is ostensibly the normalization equation
 *
 * value = (shelf life - age) - (decay rate * age)
 *
 * with caveats: because the depletion changes depending on how long an order spends on a particular
 * shelf, we need to track that history and calculate the depletion for each segment of time for
 * which the order was decaying at a particular rate.
 *
 * In other words, if an order spends 10s on the overflow shelf and it is then moved to the frozen
 * shelf, that doubled 10s of depletion doesn't magically get eradicated by a return to the frozen
 * shelf.
 *
 * Also, the equation is written such that every order depletes at a rate of at least one per
 * second IN ADDITION to the depletion that occurs due to decay rates. As such, we need to build
 * that into our cumulative equations.
 *
 * I think we could simplify this equation by adding 1 to every order's decay rate, but I'm not
 * certain.
 *
 * Everything is in seconds.
 */
@AllArgsConstructor
public class CumulativeDecayStrategy implements DecayStrategy {
  private final List<RateChange> rateChanges = new ArrayList<>();

  private final Clock clock;


  /**
   * This will add a change of decay rate, starting now. Previous changes are stored in a history
   * used for calculating remainging shelf life.
   *
   * @param rate Rate multiplier. It affects the base decay rate of an order.
   */
  @Override
  public void changeDecayRate(double rate) {
    rateChanges.add(new RateChange(clock.instant(), rate));
  }


  /**
   * This is a "predictive" calculation of what the shelf life would be if the rate were to change.
   * It's handy for figuring out a shelf life before moving an order to a different shelf.
   */
  @Override
  public long calculateRemainingShelfLifeAt(
      double baseDecayRate, long initialShelfLife, double newDecayRate) {

    val remainingShelfLife = calculateRemainingShelfLife(baseDecayRate, initialShelfLife);
    val rate = newDecayRate * baseDecayRate;
    val nextLife = remainingShelfLife / rate;
    return round(nextLife);
  }


  /**
   * Calculates the remaining shelf life at the current decay rate (which include the base rate
   * as well as the rate modifier of each shelf an order spends time on). Depletion over time at
   * a particular decay rate is tracked in segments that are all taken into account.
   */
  @Override
  public long calculateRemainingShelfLife(double baseDecayRate, long initialShelfLife) {
    Instant end = clock.instant();

    // We need to keep track of total decay for each segment for the final equation.
    double decayDepletion = 0;
    int totalDuration = 0;

    // This order is still decaying, so we must include the current decay rate in calculations. The
    // current decay rate will be the first in this list, since we're iterating backwards.
    boolean first = true;
    double currentDecayRate = 1; // If there are no rate changes, stick with the base rate.

    // In order to calculate depletion over each segment that occurred at a particular decay rate,
    // we must use the previous segment's start time as the next segment's end time. By iterating
    // through the list backwards, we don't have to keep track of end time explicitly.
    for (int i=rateChanges.size()-1; i>=0; i--) {
      val change = rateChanges.get(i);
      val decayRate = baseDecayRate * change.decayRate;

      if (first) {
        currentDecayRate = decayRate;
        first = false;
      }

      val decayDuration = Duration.between(change.start, end).getSeconds();
      if (decayDuration < 0) {
        throw new IllegalArgumentException("Time is a directed arrow!");
      }

      decayDepletion += decayDuration * decayRate;

      // Set the previous end time to the next start time.
      end = change.start;
    }

    val remainingShelfLife = initialShelfLife - totalDuration - decayDepletion;
    val remainingShelfLifeAtCurrentDecayRate = remainingShelfLife / currentDecayRate;

    long result = Math.round(remainingShelfLifeAtCurrentDecayRate); // Ditch fractions.
    return Math.max(0, result); // No negatives.
  }


  @AllArgsConstructor
  private static class RateChange {
    private final Instant start;
    private final double decayRate;
  }
}
