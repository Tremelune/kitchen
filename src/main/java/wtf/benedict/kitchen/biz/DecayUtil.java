package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.time.Duration;

import lombok.val;

// TODO How to keep track of decay when order moves from overflow queue to normal queue
class DecayUtil {
  static long getRemainingShelfLife(Clock clock, Order order, double decayRateMultiplier) {
    // How long this has been sitting on a shelf
    val decayDuration = Duration.between(order.getReceived(), clock.instant());
    double decayRate = order.getDecayRate() * decayRateMultiplier;
    double decaySeconds = decayDuration.getSeconds() * decayRate;

    if (decaySeconds < 0) {
      throw new IllegalArgumentException("Time is a directed arrow!");
    }

    double shelfLife = order.getShelfLife() - decaySeconds;
    long shelfLifeSeconds = Math.round(shelfLife);
    return Math.max(0, shelfLifeSeconds);
  }


  private DecayUtil() {
  }
}
