package wtf.benedict.kitchen.biz;

import lombok.AllArgsConstructor;

/**
 * Food that is pushed off all the shelves or isn't picked up before it expires is sent to the
 * trash. Happy birthday TO THE GROUND!
 */
@AllArgsConstructor
public class Trash {
  private final TrashListener trashListener;
  private final TrashStorage trashStorage;


  void add(Order order) {
    trashStorage.add(order);
    trashListener.onAdd(order);
  }


  interface TrashListener {
    void onAdd(Order order);
  }
}
