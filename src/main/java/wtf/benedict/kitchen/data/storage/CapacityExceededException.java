package wtf.benedict.kitchen.data.storage;

/** If we run out of space on a shelf. */
public class CapacityExceededException extends Exception {
  CapacityExceededException(int capacity) {
    super("At the maximum capacity of: " + capacity);
  }
}
