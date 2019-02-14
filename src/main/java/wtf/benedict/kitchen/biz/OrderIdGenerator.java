package wtf.benedict.kitchen.biz;

import java.util.concurrent.atomic.AtomicLong;

public class OrderIdGenerator {
  private AtomicLong id = new AtomicLong(1); // Arbitrary, but zero is just an uncommon ID...


  public long next() {
    return id.getAndIncrement();
  }
}
