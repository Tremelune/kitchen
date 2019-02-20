package wtf.benedict.kitchen.biz;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Order {
  private final long id;
  private final String name;
  private final Temperature temp;
  private final int initialShelfLife; // In seconds.
  private final double baseDecayRate;
  private final DecayStrategy decayStrategy;


  private Order(
      long id,
      String name,
      Temperature temp,
      int initialShelfLife,
      double baseDecayRate,
      DecayStrategy decayStrategy) {

    this.id = id;
    this.name = name;
    this.temp = temp;
    this.initialShelfLife = initialShelfLife;
    this.baseDecayRate = baseDecayRate;
    this.decayStrategy = decayStrategy;
  }


  void changeDecayRate(double rate) {
    decayStrategy.changeDecayRate(rate);
  }


  long calculateRemainingShelfLife() {
    return decayStrategy.calculateRemainingShelfLife(baseDecayRate, initialShelfLife);
  }

  long calculateRemainingShelfLifeAt(double decayRate) {
    return decayStrategy.calculateRemainingShelfLifeAt(baseDecayRate, initialShelfLife, decayRate);
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


  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }


  public static class Builder {
    private long id;
    private String name;
    private Temperature temp;
    private int initialShelfLife;
    private double baseDecayRate;
    private DecayStrategy decayStrategy;

    public Builder id(long id) {
      this.id = id;
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

    public Builder decayStrategy(DecayStrategy decayStrategy) {
      this.decayStrategy = decayStrategy;
      return this;
    }

    public Order build() {
      if (id == 0 // Zero could be valid, but historically entity IDs are positive.
          || name == null
          || temp == null
          || initialShelfLife == 0 // Even Soylent has a shelf life...
          || baseDecayRate == 0 // Even Spam has a decay rate...
          || decayStrategy == null) {
        throw new IllegalStateException("All values must be initialized!");
      }

      return new Order(id, name, temp, initialShelfLife, baseDecayRate, decayStrategy);
    }
  }
}
