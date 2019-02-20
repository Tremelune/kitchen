package wtf.benedict.kitchen.biz;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Trash {
  private final List<Order> orders = new ArrayList<>();

  private final TrashListener trashListener;


  void add(Order order) {
    orders.add(order);
    trashListener.onAdd(order);
  }


  List<Order> getOrders() {
    return new ArrayList<>(orders);
  }


  void reset() {
    orders.clear();
  }


  interface TrashListener {
    void onAdd(Order order);
  }
}