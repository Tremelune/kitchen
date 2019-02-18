package wtf.benedict.kitchen.app;

import java.time.Clock;

import lombok.val;
import wtf.benedict.kitchen.api.KitchenResource;
import wtf.benedict.kitchen.api.OrderGenerator;
import wtf.benedict.kitchen.api.OrderLoader;
import wtf.benedict.kitchen.biz.Kitchen;
import wtf.benedict.kitchen.biz.Storage;

/** Guice with no Guice. */
class DependencyManager {
  final KitchenResource kitchenResource;


  DependencyManager() {
    val clock = Clock.systemUTC();
    val orderLoader = new OrderLoader();
    val orderGenerator = new OrderGenerator(clock, orderLoader);
    val kitchen = new Kitchen(Storage::new);
    kitchenResource = new KitchenResource(kitchen, orderGenerator);
  }
}
