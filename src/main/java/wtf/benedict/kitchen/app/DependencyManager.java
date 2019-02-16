package wtf.benedict.kitchen.app;

import java.time.Clock;

import lombok.val;
import wtf.benedict.kitchen.api.KitchenFactory;
import wtf.benedict.kitchen.api.KitchenResource;
import wtf.benedict.kitchen.api.OrderGenerator;
import wtf.benedict.kitchen.api.OrderLoader;
import wtf.benedict.kitchen.biz.CustomerServiceClient;
import wtf.benedict.kitchen.biz.Kitchen;
import wtf.benedict.kitchen.biz.Storage;

/** Guice with no Guice. */
class DependencyManager {
  final KitchenResource kitchenResource;


  DependencyManager() {
    val clock = Clock.systemUTC();
    val orderLoader = new OrderLoader();
    val orderGenerator = new OrderGenerator(clock, orderLoader);
    kitchenResource = new KitchenResource(newKitchenFactory(), orderGenerator);
  }


  private static KitchenFactory newKitchenFactory() {
    return () -> new Kitchen(new CustomerServiceClient(), new Storage());
  }
}
