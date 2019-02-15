package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

public class DecayUtilTest {
  @Test
  public void getRemainingShelfLife() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 10); // Ten seconds have passed

    val order = Order.builder()
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    assertEquals(90, DecayUtil.getRemainingShelfLife(clock, order, 1));
  }


  @Test
  public void getRemainingShelfLife_decayRate() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 10);

    val order = Order.builder()
        .shelfLife(100)
        .decayRate(.5)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    assertEquals(95, DecayUtil.getRemainingShelfLife(clock, order, 1));
  }


  @Test
  public void getRemainingShelfLife_modifier() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 10);

    val order = Order.builder()
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    assertEquals(80, DecayUtil.getRemainingShelfLife(clock, order, 2));
  }


  @Test
  public void getRemainingShelfLife_rounding() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 10);

    // Round down from 95.1
    Order order = Order.builder()
        .shelfLife(100)
        .decayRate(.49)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    assertEquals(95, DecayUtil.getRemainingShelfLife(clock, order, 1));

    // Round up from 94.5
    order = Order.builder()
        .shelfLife(100)
        .decayRate(.55)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    assertEquals(95, DecayUtil.getRemainingShelfLife(clock, order, 1));
  }


  @Test
  public void getRemainingShelfLife_negativesConvertToZero() {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 20);

    val order = Order.builder()
        .shelfLife(10)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0)) // Over ten seconds
        .build();

    assertEquals(0, DecayUtil.getRemainingShelfLife(clock, order, 1));
  }
}
