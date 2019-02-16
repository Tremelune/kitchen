package wtf.benedict.kitchen.biz;

import lombok.val;

// Just here to demonstrate part of the value of dependency injection in Kitchen...
public class CustomerServiceClient {
  void refund(Order order) {
    val message = String.format("Refund customer! Order is too stale: %s", order);
    throw new UnsupportedOperationException(message);
  }
}
