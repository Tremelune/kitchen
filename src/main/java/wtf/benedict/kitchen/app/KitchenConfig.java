package wtf.benedict.kitchen.app;

import io.dropwizard.Configuration;

/** Required by DropWizard. Seemed like a fine place for challenge-specified constants. */
class KitchenConfig extends Configuration {
  static final int SHELF_CAPACITY = 15;
  static final int OVERFLOW_CAPACITY = 20;

  static final int DECAY_RATE = 1;
  static final int OVERFLOW_DECAY_RATE = 2;

  static final int DRIVER_DELAY_MIN_SECONDS = 2;
  static final int DRIVER_DELAY_MAX_SECONDS = 10;
}
