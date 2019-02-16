package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.AllArgsConstructor;
import lombok.val;

public class Order {
  private final List<RateChange> rateChanges = new ArrayList<>();

  private final long id;
  private final Clock clock;
  private final String name;
  private final Temperature temp;
  private final int initialShelfLife; // In seconds.
  private final double baseDecayRate;


  private Order(
      long id,
      Clock clock,
      String name,
      Temperature temp,
      int initialShelfLife,
      double baseDecayRate) {
    this.id = id;
    this.clock = clock;
    this.name = name;
    this.temp = temp;
    this.initialShelfLife = initialShelfLife;
    this.baseDecayRate = baseDecayRate;
  }


  void changeDecayRate(double rate) {
    rateChanges.add(new RateChange(clock.instant(), rate));
  }


  long calculateRemainingShelfLife() {
    // In order to calculate decay over each segment that occurred at a particular rate, we need to
    // used the previous segment's start time as the next segment's end time. By iterating through
    // this list backwards, we don't have to keep track of end time explicitly.
    double decay = 0;
    Instant end = clock.instant();
    for (int i=rateChanges.size()-1; i>=0; i--) {
      val change = rateChanges.get(i);
      decay += calculateDecaySince(change, end);
      end = change.start;
    }

    val remainingShelfLife = initialShelfLife - decay;
    return Math.round(remainingShelfLife);
  }


  long getId() {
    return id;
  }

  String getName() {
    return name;
  }

  Temperature getTemp() {
    return temp;
  }

  // Decay in seconds since this change was made
  private double calculateDecaySince(RateChange change, Instant end) {
    val decayRate = baseDecayRate * change.decayRate;
    val decayDuration = Duration.between(change.start, end);
    double decay = decayDuration.getSeconds() * decayRate;

    if (decay < 0) {
      throw new IllegalArgumentException("Time is a directed arrow!");
    }

    return decay;
  }


  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }


  public static class Builder {
    private long id;
    private Clock clock;
    private String name;
    private Temperature temp;
    private int initialShelfLife;
    private double baseDecayRate;

    public Builder id(long id) {
      this.id = id;
      return this;
    }

    public Builder clock(Clock clock) {
      this.clock = clock;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder temp(Temperature temp) {
      this.temp = temp;
      return this;
    }

    public Builder initialShelfLife(int initialShelfLife) {
      this.initialShelfLife = initialShelfLife;
      return this;
    }

    public Builder baseDecayRate(double baseDecayRate) {
      this.baseDecayRate = baseDecayRate;
      return this;
    }

    public Order build() {
      if (id == 0 // Zero could be valid, but historically entity IDs are positive.
          || clock == null
          || name == null
          || temp == null
          || initialShelfLife == 0 // Even Soylent has a shelf life...
          || baseDecayRate == 0) { // Even Spam has a decay rate...
        throw new IllegalStateException("All values must be initialized!");
      }

      return new Order(id, clock, name, temp, initialShelfLife, baseDecayRate);
    }
  }


  @AllArgsConstructor
  private static class RateChange {
    private final Instant start;
    private final double decayRate;
  }
}
