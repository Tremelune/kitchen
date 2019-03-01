package wtf.benedict.kitchen.data.storage;

import java.util.ArrayList;
import java.util.List;

import wtf.benedict.kitchen.data.Order;

/** Stores the state of the trash. */
public class TrashStorage {
  private final List<Order> orders = new ArrayList<>();


  public void add(Order order) {
    orders.add(order);
  }


  public List<Order> getAll() {
    return new ArrayList<>(orders);
  }


  public void deleteAll() {
    orders.clear();
  }
}
