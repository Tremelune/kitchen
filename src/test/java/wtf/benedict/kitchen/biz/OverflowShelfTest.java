package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static wtf.benedict.kitchen.biz.Order.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Order.Temperature.HOT;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.test.TestUtil;

public class OverflowShelfTest {
  @Test
  public void pushAndPullShouldTrackSize() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val underTest = new OverflowShelf(1, clock);

    val freshOrder = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .id(11)
        .temp(COLD)
        .shelfLife(10)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    // Push and pull a few to make sure the size is tracked accurately.
    underTest.put(freshOrder);
    assertEquals(freshOrder, underTest.pull(freshOrder.getTemp()));
    assertNull(underTest.pull(freshOrder.getTemp()));
    underTest.put(staleOrder);
    assertEquals(staleOrder, underTest.pull(staleOrder.getTemp()));
    underTest.put(freshOrder);
    underTest.put(staleOrder);
    assertEquals(freshOrder, underTest.pull(freshOrder.getTemp()));
    assertNull(underTest.pull(freshOrder.getTemp()));
  }


  @Test(expected = StaleOrderException.class)
  public void overflowShouldRejectNewOrderIfItsStalest() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val underTest = new OverflowShelf(1, clock);

    val freshOrder = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .id(11)
        .temp(COLD)
        .shelfLife(10)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    underTest.put(freshOrder);
    underTest.put(staleOrder);
  }


  @Test
  public void overflowShouldDiscardStalest() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 1);
    val underTest = new OverflowShelf(1, clock);

    val freshOrder = Order.builder()
        .id(10)
        .temp(HOT)
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val staleOrder = Order.builder()
        .id(11)
        .temp(COLD)
        .shelfLife(10)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    underTest.put(staleOrder);
    underTest.put(freshOrder);

    assertEquals(freshOrder, underTest.pull(freshOrder.getTemp()));
    assertNull(underTest.pull(staleOrder.getTemp()));
  }
}
