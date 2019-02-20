package wtf.benedict.kitchen.biz;

import java.util.List;

import net.jodah.expiringmap.ExpirationListener;

public interface StorageFactory {
  Storage newStorage(ExpirationListener<Long, Order> expirationListener, List<Order> trashedOrders);
}
