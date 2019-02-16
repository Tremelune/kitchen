package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.val;

// Shelf life calculations are not accurate to the milli.
// TODO Test.
public class Order {
  private final List<RateChange> rateChanges = new ArrayList<>();

  private final long id;
  private final Clock clock;
  private final String name;
  private final Temperature temp;
  private final int initialShelfLife; // In seconds.
  private final double baseDecayRate;


  public Order(
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


  public void changeDecayRate(double rate) {
    rateChanges.add(new RateChange(clock.instant(), rate));
  }


  public long calculateRemainingShelfLife() {
    double decay = 0;
    for (RateChange change : rateChanges) {
      decay += calculateDecaySince(change);
    }

    val remainingShelfLife = initialShelfLife - decay;
    return Math.round(remainingShelfLife);
  }


  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Temperature getTemp() {
    return temp;
  }

  // Decay in seconds since this change was made
  private double calculateDecaySince(RateChange change) {
    val decayRate = baseDecayRate * change.decayRate;
    val decayDuration = Duration.between(change.start, clock.instant());
    double decay = decayDuration.getSeconds() * decayRate;

    if (decay < 0) {
      throw new IllegalArgumentException("Time is a directed arrow!");
    }

    return decay;
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
