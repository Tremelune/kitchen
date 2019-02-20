package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.time.Clock;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

public class OrderTest {
  @Test
  public void calculateRemainingShelfLife() {
    val clock = mock(Clock.class);
    when(clock.instant()).thenReturn(TestUtil.instant(2019, 1, 1, 0, 0, 0));

    val order = newOrder(clock, 5, 100);
    assertEquals(100, order.calculateRemainingShelfLife());

    order.changeDecayRate(3);
    when(clock.instant()).thenReturn(TestUtil.instant(2019, 1, 1, 0, 0, 1));
    assertEquals(6, order.calculateRemainingShelfLife()); // One second has "passed"

    order.changeDecayRate(1);
    when(clock.instant()).thenReturn(TestUtil.instant(2019, 1, 1, 0, 0, 2));
    assertEquals(16, order.calculateRemainingShelfLife()); // Two seconds have "passed"
  }


  private static Order newOrder(Clock clock, double baseDecayRate, int initialShelfLife) {
    return new Order.Builder()
        .id(10)
        .name("name")
        .temp(HOT)
        .baseDecayRate(baseDecayRate)
        .initialShelfLife(initialShelfLife)
        .decayStrategy(new CumulativeDecayStrategy(clock))
        .build();
  }
}