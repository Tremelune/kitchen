package wtf.benedict.kitchen.app;

import wtf.benedict.kitchen.api.ShelfResource;

/** Guice with no Guice. */
class DependencyManager {
  final ShelfResource shelfResource;


  DependencyManager() {
    shelfResource = new ShelfResource();
  }
}
