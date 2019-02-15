package wtf.benedict.kitchen.biz;

import static wtf.benedict.kitchen.biz.Order.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Order.Temperature.HOT;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.test.TestUtil;

public class OverflowShelfTest {
  @Test(expected = StaleOrderException.class)
  public void overflowShouldRejectNewOrderIfItsStalest() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val underTest = new OverflowShelf(1, clock);

    val freshOrder = Order.builder()
        .id(10)
        .name("fresh")
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .id(11)
        .name("fresh")
        .shelfLife(10)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    underTest.put(HOT, freshOrder);
    underTest.put(COLD, staleOrder);
  }
}
