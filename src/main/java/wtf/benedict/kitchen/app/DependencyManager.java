package wtf.benedict.kitchen.app;

import static wtf.benedict.kitchen.app.KitchenConfig.DECAY_RATE;
import static wtf.benedict.kitchen.app.KitchenConfig.OVERFLOW_CAPACITY;
import static wtf.benedict.kitchen.app.KitchenConfig.OVERFLOW_DECAY_RATE;
import static wtf.benedict.kitchen.app.KitchenConfig.SHELF_CAPACITY;

import java.time.Clock;

import lombok.val;
import wtf.benedict.kitchen.api.KitchenResource;
import wtf.benedict.kitchen.api.OrderGenerator;
import wtf.benedict.kitchen.api.OrderLoader;
import wtf.benedict.kitchen.biz.DriverDepot;
import wtf.benedict.kitchen.biz.DriverExpirationListener;
import wtf.benedict.kitchen.data.storage.DriverStorage;
import wtf.benedict.kitchen.biz.DriverTrashListener;
import wtf.benedict.kitchen.biz.Kitchen;
import wtf.benedict.kitchen.biz.OverflowBalancer;
import wtf.benedict.kitchen.biz.OverflowShelf;
import wtf.benedict.kitchen.data.storage.ShelfStorage;
import wtf.benedict.kitchen.biz.StorageAggregator;
import wtf.benedict.kitchen.biz.StorageResetter;
import wtf.benedict.kitchen.biz.TemperatureShelf;
import wtf.benedict.kitchen.biz.Trash;
import wtf.benedict.kitchen.data.storage.TrashStorage;

/**
 * Builds the dependency graph for the system.
 *
 * Guice without having to deal with Guice or another heavy dependency injection framework.
 */
class DependencyManager {
  final KitchenResource kitchenResource;


  DependencyManager() {
    val clock = Clock.systemUTC();
    val driverStorage = new DriverStorage();
    val driverDepot = new DriverDepot(clock, driverStorage);
    val driverTrashListener = new DriverTrashListener(driverDepot);
    val trashStorage = new TrashStorage();
    val trash = new Trash(driverTrashListener, trashStorage);
    val driverExpirationListener = new DriverExpirationListener(driverDepot, trash);

    // Shelves are stateful non-singletons.
    val overflowStorage = new ShelfStorage(OVERFLOW_CAPACITY, OVERFLOW_DECAY_RATE, driverExpirationListener);
    val shelfStorage = new ShelfStorage(SHELF_CAPACITY, DECAY_RATE, driverExpirationListener);

    val overflowShelf = new OverflowShelf(overflowStorage, trash, OVERFLOW_CAPACITY);
    val overflowBalancer = new OverflowBalancer(overflowShelf, trash);
    val shelf = new TemperatureShelf(OVERFLOW_DECAY_RATE, overflowBalancer, overflowShelf, shelfStorage);
    val kitchen = new Kitchen(driverDepot, shelf, trash);
    val storageAggregator = new StorageAggregator(driverStorage, overflowStorage, shelfStorage, trashStorage);
    val storageResetter = new StorageResetter(driverStorage, overflowStorage, shelfStorage, trashStorage);
    val orderLoader = new OrderLoader();
    val orderGenerator = new OrderGenerator(clock, orderLoader);

    // One resource to rule them all...
    kitchenResource = new KitchenResource(kitchen, storageAggregator, storageResetter, orderGenerator);
  }
}
