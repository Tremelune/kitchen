package wtf.benedict.box.app;

import com.codahale.metrics.health.HealthCheck;

public class KitchenHealthCheck extends HealthCheck {
  @Override
  protected Result check() {
    return Result.healthy();
  }
}
