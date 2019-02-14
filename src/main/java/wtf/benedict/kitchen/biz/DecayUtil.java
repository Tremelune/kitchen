package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.time.Duration;

import lombok.val;

// TODO How to keep track of decay when order moves from overflow queue to normal queue
class DecayUtil {
  static long getRemainingShelfLife(Clock clock, Order order, double decayRateMultiplier) {
    // How long this has been sitting on a shelf
    val decayDuration = Duration.between(clock.instant(), order.getReceived());
    double decayRate = order.getDecayRate() * decayRateMultiplier;
    double decaySeconds = decayDuration.getSeconds() * decayRate;
    double shelfLife = order.getShelfLife() - decaySeconds;
    long shelfLifeSeconds = Math.round(shelfLife);
    return Math.max(0, shelfLifeSeconds);
  }


  private DecayUtil() {
  }
}
