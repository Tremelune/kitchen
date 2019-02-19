package wtf.benedict.kitchen.biz;

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
  public long calculateRemainingShelfLife(double baseDecayRate, long initialShelfLife) {
    // In order to calculate decay over each segment that occurred at a particular rate, we need to
    // used the previous segment's start time as the next segment's end time. By iterating through
    // this list backwards, we don't have to keep track of end time explicitly.
    double decay = 0;
    Instant end = clock.instant();
    for (int i=rateChanges.size()-1; i>=0; i--) {
      val change = rateChanges.get(i);
      decay += calculateDecaySince(baseDecayRate, change, end);
      end = change.start;
    }

    val remainingShelfLife = initialShelfLife - decay;
    return Math.round(remainingShelfLife);
  }


  // Decay in seconds since this change was made
  private double calculateDecaySince(double baseDecayRate, RateChange change, Instant end) {
    val decayRate = baseDecayRate * change.decayRate;
    val decayDuration = Duration.between(change.start, end);
    double decay = decayDuration.getSeconds() * decayRate;

    if (decay < 0) {
      throw new IllegalArgumentException("Time is a directed arrow!");
    }

    return decay;
  }


  @AllArgsConstructor
  private static class RateChange {
    private final Instant start;
    private final double decayRate;
  }
}
