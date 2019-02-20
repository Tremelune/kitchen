package wtf.benedict.kitchen.app;

import com.codahale.metrics.health.HealthCheck;

/** DropWizard spits a big warning if this isn't here. */
public class KitchenHealthCheck extends HealthCheck {
  @Override
  protected Result check() {
    return Result.healthy();
  }
}
