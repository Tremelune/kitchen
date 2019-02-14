package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.Shelf.OverflowException;
import wtf.benedict.kitchen.test.TestUtil;

public class ShelfTest {
  @Test
  public void expirationOverTime() throws Exception {
    val orderA = Order.builder()
        .id(10)
        .name("a")
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();
    
    val orderB = Order.builder()
        .id(11)
        .name("b")
        .shelfLife(1)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0))
        .build();

    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0); // One second from orders received
    val underTest = new Shelf(clock, 2, 1);

    underTest.put(orderA);
    underTest.put(orderB);

    assertEquals("a", underTest.get(10).getName());
    assertEquals("b", underTest.get(11).getName());

    // Check that the order with 1s TTL has been expired.
    Thread.sleep(1000);
    assertEquals("a", underTest.get(10).getName());
    assertNull(underTest.get(11));
  }


  @Test(expected = NullPointerException.class)
  public void nullsShouldBeRejected() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);
    val underTest = new Shelf(clock, 1, 1);

    underTest.put(null);
  }


  /**
   * This test are time-sensitive, so the setup is to have the order have been received 60s ago,
   * with a shelf life of 100s.
   */
  @Test(expected = OverflowException.class)
  public void explodeOnOverflow() throws Exception {
    val clock = TestUtil.clock(2019, 1, 1, 0, 0, 0);
    val underTest = new Shelf(clock, 1, 1);
    underTest.put(newOrder());
    underTest.put(newOrder());
  }


  // Mainly exists to avoid null errors in the tests unrelated to decay.
  private static Order newOrder() {
    return Order.builder()
        .shelfLife(100)
        .decayRate(1)
        .received(TestUtil.instant(2019, 1, 1, 0, 0, 0)) // We need some shelf life for some tests.
        .build();
  }
}
