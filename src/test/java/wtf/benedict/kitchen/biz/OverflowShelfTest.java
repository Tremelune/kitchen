package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.data.Temperature.COLD;
import static wtf.benedict.kitchen.data.Temperature.HOT;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.storage.ShelfStorage;
import wtf.benedict.kitchen.data.Temperature;
import wtf.benedict.kitchen.test.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class OverflowShelfTest {
  @Mock
  private ExpirationListener<Long, Order> expirationListener;
  @Mock
  private Trash trash;

  private OverflowShelf underTest;


  @Before
  public void setUp() {
    val shelfStorage = new ShelfStorage(10, 1, expirationListener);
    underTest = new OverflowShelf(shelfStorage, trash, 10);
  }


  @Test
  public void pull_byOrderId() throws Exception {
    val fresh = newOrder(10, HOT, 200);
    val stale = newOrder(11, COLD, 100);

    underTest.put(fresh);
    underTest.put(stale);
    assertEquals(fresh, underTest.pull(10));
  }


  @Test
  public void pushAndPullShouldTrackSize() throws Exception {
    val fresh = newOrder(10, HOT, 200);
    val stale = newOrder(11, COLD, 100);

    // Push and pull a few to make sure the size is tracked accurately.
    underTest.put(fresh);
    assertEquals(fresh, underTest.pullStalest(HOT));
    assertNull(underTest.pull(10));
    underTest.put(stale);
    assertEquals(stale, underTest.pullStalest(COLD));
    underTest.put(fresh);
    underTest.put(stale);
    assertEquals(fresh, underTest.pullStalest(HOT));
    assertNull(underTest.pullStalest(HOT));
  }


  @Test
  public void overflowShouldDiscardStalest() throws Exception {
    // Easiest way to reduce capacity to 1
    val shelfStorage = new ShelfStorage(1, 1, expirationListener);
    val underTest = new OverflowShelf(shelfStorage, trash, 1);

    val fresh = newOrder(10, HOT, 200);
    val stale = newOrder(11, COLD, 100);

    underTest.put(stale);
    underTest.put(fresh);

    assertEquals(fresh, underTest.pullStalest(HOT));
    assertNull(underTest.pullStalest(COLD));
  }


  private static Order newOrder(long id, Temperature temp, int initialShelfLife) {
    return new Order.Builder()
        .id(id)
        .name("name")
        .temp(temp)
        .baseDecayRate(1)
        .initialShelfLife(initialShelfLife)
        .decayStrategy(new CumulativeDecayStrategy(newClock()))
        .build();
  }


  private static Clock newClock() {
    val one = TestUtil.instant(2019, 1, 1, 0, 0, 0);
    val two = TestUtil.instant(2019, 1, 1, 0, 0, 1);

    val clock = mock(Clock.class);
    when(clock.instant()).thenReturn(one, two);
    return clock;
  }
}
