package wtf.benedict.kitchen.api;

import static org.junit.Assert.assertEquals;
import static wtf.benedict.kitchen.biz.Order.Temperature.FROZEN;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.OrderIdGenerator;
import wtf.benedict.kitchen.test.TestUtil;

public class OrderLoaderTest {
  @Test
  public void next() {
    val clock = TestUtil.clock(2019, 1, 1, 1, 1, 1);

    val underTest = new OrderLoader(clock, new OrderIdGenerator());

    val order = underTest.next();

    assertEquals(1, order.getId());
    assertEquals("Banana Split", order.getName());
    assertEquals(FROZEN, order.getTemp());
    assertEquals(20, order.getShelfLife());
    assertEquals(0.63, order.getDecayRate(), 0);
    assertEquals(clock.instant(), order.getReceived());
  }
}
