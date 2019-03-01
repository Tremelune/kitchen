package wtf.benedict.kitchen.biz.kitchen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.data.Temperature.COLD;
import static wtf.benedict.kitchen.data.Temperature.FROZEN;
import static wtf.benedict.kitchen.data.Temperature.HOT;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.CumulativeDecayStrategy;
import wtf.benedict.kitchen.biz.kitchen.OverflowBalancer;
import wtf.benedict.kitchen.biz.kitchen.OverflowShelf;
import wtf.benedict.kitchen.biz.kitchen.TemperatureShelf;
import wtf.benedict.kitchen.biz.kitchen.Trash;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.storage.ShelfStorage;
import wtf.benedict.kitchen.data.Temperature;
import wtf.benedict.kitchen.test.TestUtil;



@RunWith(MockitoJUnitRunner.class)
public class TemperatureShelfTest {
  @Mock
  private ExpirationListener<Long, Order> expirationListener;
  @Mock
  private Trash trash;

  private OverflowShelf overflowShelf;
  private TemperatureShelf underTest;


  @Before
  public void setUp() {
    val shelfStorage = new ShelfStorage(1, 1, expirationListener);
    val overflowStorage = new ShelfStorage(10, 2, expirationListener);
    overflowShelf = new OverflowShelf(overflowStorage, trash, 20);
    val overflowBalancer = new OverflowBalancer(overflowShelf, trash);
    underTest = new TemperatureShelf(0, overflowBalancer, overflowShelf, shelfStorage);
  }


  @Test
  public void put() throws Exception {
    val order = newOrder(10, 100);

    underTest.put(order);

    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void put_shouldOverflowFreshestOntoOverflowShelf() throws Exception {
    val fresh = newOrder(10, 1000);
    val stale = newOrder(11, 100);
    val freshest = newOrder(12, 2000);

    underTest.put(fresh);
    underTest.put(stale);
    underTest.put(freshest);

    assertEquals(stale, overflowShelf.pullStalest(HOT));
    assertEquals(fresh, overflowShelf.pullStalest(HOT));
    assertNull(overflowShelf.pullStalest(HOT));
    assertNull(underTest.pull(10));
    assertNull(underTest.pull(11));
    assertEquals(freshest, underTest.pull(12));
  }


  @Test
  public void put_shouldOverflowWithLowerDecayRate() throws Exception {
    val lowDecay = newOrder(10, 1, 100); // Lasts 50s on overflow
    val highDecay = newOrder(11, 10, 100); // Lasts 5s on overflow

    underTest.put(lowDecay);
    underTest.put(highDecay);

    assertEquals(lowDecay, overflowShelf.pullStalest(HOT));
  }


  @Test
  public void pull() throws Exception {
    val order = newOrder(10, 100);

    assertNull(underTest.pull(10));
    underTest.put(order);

    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void pull_pullsFromOverflow() throws Exception {
    val order = newOrder(10, 1000);

    assertNull(underTest.pull(10));
    overflowShelf.put(order);
    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void pull_pullsFromOverflowWhenSpaceBecomesAvailable() throws Exception {
    val order = newOrder(10, 1000);
    val overflow = newOrder(11, 1000);

    underTest.put(order);
    overflowShelf.put(overflow);

    assertEquals(order, underTest.pull(10));
    assertNull(overflowShelf.pull(11));
    assertEquals(overflow, underTest.pull(11));
  }


  @Test
  public void orderShouldBeFoundById() throws Exception {
    val hot = newOrder(10, HOT);
    val cold = newOrder(11, COLD);
    val frozen = newOrder(12, FROZEN);

    underTest.put(hot);
    underTest.put(cold);
    underTest.put(frozen);

    assertNull(underTest.pull(1337));
    assertEquals(hot, underTest.pull(10));
    assertEquals(cold, underTest.pull(11));
    assertEquals(frozen, underTest.pull(12));
  }


  private static Order newOrder(long id, int initialShelfLife) {
    return newOrder(id, 1, initialShelfLife);
  }

  private static Order newOrder(long id, double baseDecayRate, int initialShelfLife) {
    return new Order.Builder()
        .id(id)
        .name("name")
        .temp(HOT)
        .baseDecayRate(baseDecayRate)
        .initialShelfLife(initialShelfLife)
        .decayStrategy(new CumulativeDecayStrategy(newClock()))
        .build();
  }

  private static Order newOrder(long id, Temperature temp) {
    return new Order.Builder()
        .id(id)
        .name("name")
        .temp(temp)
        .baseDecayRate(1)
        .initialShelfLife(100)
        .decayStrategy(new CumulativeDecayStrategy(TestUtil.clock(2019, 1, 1, 0, 0, 0)))
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
