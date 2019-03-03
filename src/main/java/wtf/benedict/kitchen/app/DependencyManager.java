package wtf.benedict.kitchen.app;

import static wtf.benedict.kitchen.app.KitchenConfig.DECAY_RATE;
import static wtf.benedict.kitchen.app.KitchenConfig.DRIVER_DELAY_MAX_SECONDS;
import static wtf.benedict.kitchen.app.KitchenConfig.DRIVER_DELAY_MIN_SECONDS;
import static wtf.benedict.kitchen.app.KitchenConfig.OVERFLOW_CAPACITY;
import static wtf.benedict.kitchen.app.KitchenConfig.OVERFLOW_DECAY_RATE;
import static wtf.benedict.kitchen.app.KitchenConfig.SHELF_CAPACITY;
import static wtf.benedict.kitchen.app.KitchenConfig.THREAD_POOL_SIZE;

import java.time.Clock;
import java.util.concurrent.Executors;

import lombok.val;
import wtf.benedict.kitchen.api.KitchenResource;
import wtf.benedict.kitchen.api.OrderGenerator;
import wtf.benedict.kitchen.api.OrderLoader;
import wtf.benedict.kitchen.biz.StorageAggregator;
import wtf.benedict.kitchen.biz.StorageResetter;
import wtf.benedict.kitchen.biz.delivery.DriverDepot;
import wtf.benedict.kitchen.biz.delivery.DriverExpirationListener;
import wtf.benedict.kitchen.biz.delivery.OrderExpirer;
import wtf.benedict.kitchen.biz.kitchen.Kitchen;
import wtf.benedict.kitchen.biz.kitchen.OverflowShelf;
import wtf.benedict.kitchen.biz.kitchen.Shelf;
import wtf.benedict.kitchen.biz.kitchen.ShelfStorageFactory;
import wtf.benedict.kitchen.biz.kitchen.Trash;
import wtf.benedict.kitchen.data.storage.DriverStorage;
import wtf.benedict.kitchen.data.storage.ShelfStorage;
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
    val driverDepot = new DriverDepot(clock, driverStorage, DRIVER_DELAY_MIN_SECONDS, DRIVER_DELAY_MAX_SECONDS);
    val trashStorage = new TrashStorage();
    val trash = new Trash(driverStorage, trashStorage);
    val orderExpirer = new OrderExpirer(driverStorage, trash);
    val driverExpirationListener = new DriverExpirationListener(orderExpirer);
    val overflowStorage = new ShelfStorage(OVERFLOW_CAPACITY, OVERFLOW_DECAY_RATE, driverExpirationListener);
    val overflowShelf = new OverflowShelf(overflowStorage, trash, OVERFLOW_CAPACITY);
    val shelfStorageFactory = new ShelfStorageFactory(SHELF_CAPACITY, DECAY_RATE);
    val shelf = new Shelf(OVERFLOW_DECAY_RATE, orderExpirer, overflowShelf, shelfStorageFactory, trash);
    val executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    val kitchen = new Kitchen(driverDepot, executorService, shelf, trash);
    val storageAggregator = new StorageAggregator(driverStorage, overflowStorage, shelf.getShelfStorage(), trashStorage);
    val storageResetter = new StorageResetter(driverStorage, overflowStorage, shelf.getShelfStorage(), trashStorage);
    val orderLoader = new OrderLoader();
    val orderGenerator = new OrderGenerator(clock, orderLoader);

    // One resource to rule them all...
    kitchenResource = new KitchenResource(kitchen, storageAggregator, storageResetter, orderGenerator);
  }
}
