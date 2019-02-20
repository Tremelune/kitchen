package wtf.benedict.kitchen.biz;

import net.jodah.expiringmap.ExpirationListener;

public interface StorageFactory {
  Storage newStorage(ExpirationListener<Long, Order> expirationListener, Trash trash);
}
