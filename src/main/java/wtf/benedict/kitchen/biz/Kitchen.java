package wtf.benedict.kitchen.biz;

import lombok.AllArgsConstructor;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;

// TODO Cancel drivers.
// TODO Move overflow stuff when stuff is evicted from temp shelves.
// TODO Per-order decay strategy.
// TODO Display.
// TODO Enterprisize. Event sourcing...caching...message bus...CQRS...nine microservices...
@AllArgsConstructor
class Kitchen {
  private final CustomerServiceClient customerServiceClient;
  private final DeliveryDepot deliveryDepot;
  private final Storage storage;


  void receiveOrder(Order order) {
    try {
      storage.put(order);
    } catch (StaleOrderException e) {
      customerServiceClient.refund(order);
    }

    deliveryDepot.dispatchDriver(order.getId());
  }


  Order pickupOrder(long orderId) {
    return storage.pull(orderId);
  }
}
