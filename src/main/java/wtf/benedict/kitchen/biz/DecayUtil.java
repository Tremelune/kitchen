package wtf.benedict.kitchen.biz;

import java.time.Clock;
import java.time.Duration;

import lombok.val;

class DecayUtil {
  static long getRemainingShelfLife(Clock clock, Order order) {
    val decayDuration = Duration.between(clock.instant(), order.getReceived());
    double decaySeconds = decayDuration.getSeconds() * order.getDecayRate();
    double shelfLife = order.getShelfLife() - decaySeconds;
    long shelfLifeSeconds = Math.round(shelfLife);
    return Math.max(0, shelfLifeSeconds);
  }


  private DecayUtil() {
  }
}
