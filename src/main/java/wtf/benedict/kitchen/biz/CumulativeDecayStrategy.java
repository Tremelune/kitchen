package wtf.benedict.kitchen.biz;

import static java.lang.Math.round;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class CumulativeDecayStrategy implements DecayStrategy {
  private final List<RateChange> rateChanges = new ArrayList<>();

  private final Clock clock;


  @Override
  public void modifyDecayRate(double rate) {
    rateChanges.add(new RateChange(clock.instant(), rate));
  }


  @Override
  public long calculateRemainingShelfLifeAt(
      double baseDecayRate, long initialShelfLife, double newDecayRate) {

    val remainingShelfLife = calculateRemainingShelfLife(baseDecayRate, initialShelfLife);
    val rate = newDecayRate * baseDecayRate;
    val nextLife = remainingShelfLife / rate;
    return round(nextLife);
  }


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
  @Override
  public long calculateRemainingShelfLife(double baseDecayRate, long initialShelfLife) {
    // In order to calculate depletion over each segment that occurred at a particular decay rate,
    // we must use the previous segment's start time as the next segment's end time. By iterating
    // through the list backwards, we don't have to keep track of end time explicitly.
    Instant end = clock.instant();

    // We need to keep track of total decay for each segment for the final equation.
    double decayDepletion = 0;
    int totalDuration = 0;

    // This order is still decaying, so we must include the current decay rate in calculations. The
    // current decay rate will be the first in this list, since we're iterating backwards.
    boolean first = true;
    double currentDecayRate = 1; // If there are no rate changes, stick with the base rate.

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
