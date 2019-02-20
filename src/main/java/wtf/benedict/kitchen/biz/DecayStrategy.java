package wtf.benedict.kitchen.biz;

interface DecayStrategy {
  /** Sets a new decay rate moving forward. */
  void changeDecayRate(double rate);

  /** Calculates remaining shelf life at the current decay rate (in seconds). */
  long calculateRemainingShelfLife(double baseRate, long initialShelfLife);

  /** Calculates remaining shelf life for newRate (in seconds). */
  long calculateRemainingShelfLifeAt(double baseRate, long initialShelfLife, double newRate);
}
