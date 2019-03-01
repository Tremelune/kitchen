package wtf.benedict.kitchen.biz;

import lombok.AllArgsConstructor;
import wtf.benedict.kitchen.data.storage.DriverStorage;
import wtf.benedict.kitchen.data.storage.ShelfStorage;
import wtf.benedict.kitchen.data.storage.TrashStorage;

/** Resets stored state. */
@AllArgsConstructor
public class StorageResetter {
  private final DriverStorage driverStorage;
  private final ShelfStorage overflowStorage;
  private final ShelfStorage shelfStorage;
  private final TrashStorage trashStorage;


  /** Resets drivers, shelves, and trash. */
  public void reset() {
    driverStorage.deleteAll();
    shelfStorage.deleteAll();
    overflowStorage.deleteAll();
    trashStorage.deleteAll();
  }
}
