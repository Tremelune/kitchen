package wtf.benedict.kitchen.biz;

import net.jodah.expiringmap.ExpirationListener;

/** This is just a hook to facilitate resetting state from and endpoint. */
public interface StorageFactory {
  Storage newStorage(ExpirationListener<Long, Order> expirationListener, Trash trash);
}
