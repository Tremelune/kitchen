package wtf.benedict.box.app;

import wtf.benedict.box.api.ShelfResource;

/** Guice with no Guice. */
class DependencyManager {
  final ShelfResource shelfResource;


  DependencyManager() {
    shelfResource = new ShelfResource();
  }
}
