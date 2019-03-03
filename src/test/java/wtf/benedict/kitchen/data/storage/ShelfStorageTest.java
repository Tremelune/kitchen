package wtf.benedict.kitchen.data.storage;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static wtf.benedict.kitchen.data.Temperature.COLD;
import static wtf.benedict.kitchen.data.Temperature.HOT;
import static wtf.benedict.kitchen.test.TestUtil.assertSize;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.CumulativeDecayStrategy;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.Temperature;
import wtf.benedict.kitchen.test.TestUtil;



@RunWith(MockitoJUnitRunner.class)
public class ShelfStorageTest {
  @Mock
  private ExpirationListener<Long, Order> expirationListener;

  private ShelfStorage underTest;


  @Before
  public void setUp() {
    underTest = new ShelfStorage(10, 1, expirationListener);
  }


  @Test
  public void put() throws Exception {
    val hot = newOrder(10, 100, HOT);
    val cold = newOrder(11, 100, COLD);

    underTest.put(hot);
    underTest.put(cold);

    assertEquals(hot, underTest.getAll(HOT).get(0));
    assertEquals(cold, underTest.getAll(COLD).get(0));
  }


  @Test
  public void pull() throws Exception {
    val order = newOrder(10, 100, HOT);

    assertNull(underTest.pull(10));
    underTest.put(order);

    assertEquals(order, underTest.pull(10));
  }


  @Test
  public void getAndPullStalest() throws Exception {
    val hotStale = newOrder(10, 100, HOT);
    val hotFresh = newOrder(11, 200, HOT);
    val coldStale = newOrder(12, 100, COLD);
    val coldFresh = newOrder(13, 200, COLD);

    underTest.put(hotStale);
    underTest.put(hotFresh);
    underTest.put(coldStale);
    underTest.put(coldFresh);

    assertEquals(hotStale, underTest.getStalest(HOT));
    assertEquals(hotStale, underTest.pullStalest(HOT));
    assertEquals(hotFresh, underTest.pullStalest(HOT));
    assertEquals(coldStale, underTest.getStalest(COLD));
    assertEquals(coldStale, underTest.pullStalest(COLD));
    assertEquals(coldFresh, underTest.pullStalest(COLD));
  }


  @Test
  public void getAndPullFreshest() throws Exception {
    val hotStale = newOrder(10, 100, HOT);
    val hotFresh = newOrder(11, 200, HOT);
    val coldStale = newOrder(12, 100, COLD);
    val coldFresh = newOrder(13, 200, COLD);

    underTest.put(hotStale);
    underTest.put(hotFresh);
    underTest.put(coldStale);
    underTest.put(coldFresh);

    assertEquals(hotFresh, underTest.getFreshest(HOT));
    assertEquals(coldFresh, underTest.getFreshest(COLD));
  }


  @Test
  public void getAll() throws Exception {
    val a = newOrder(10, 100, HOT);
    val b = newOrder(11, 200, COLD);

    assertSize(0, underTest.getAll());
    underTest.put(a);
    underTest.put(b);
    assertSize(2, underTest.getAll());
    assertEquals(a, underTest.getAll().get(0));
    assertEquals(b, underTest.getAll().get(1));
    assertEquals(a, underTest.getAll(HOT).get(0));
    assertEquals(b, underTest.getAll(COLD).get(0));
  }


  @Test
  public void countAll() throws Exception {
    val a = newOrder(10, 100, HOT);
    val b = newOrder(11, 200, COLD);

    assertEquals(0, underTest.countAll());
    underTest.put(a);
    assertEquals(1, underTest.countAll());
    underTest.put(b);
    assertEquals(2, underTest.countAll());
  }


  @Test
  public void deleteAll() throws Exception {
    val a = newOrder(10, 100, HOT);
    val b = newOrder(11, 200, COLD);

    underTest.put(a);
    underTest.put(b);
    assertEquals(2, underTest.countAll());
    underTest.deleteAll();
    assertEquals(0, underTest.countAll());
  }


  private static Order newOrder(long id, int initialShelfLife, Temperature temp) {
    return new Order.Builder()
        .id(id)
        .name("name")
        .temp(temp)
        .baseDecayRate(1)
        .initialShelfLife(initialShelfLife)
        .decayStrategy(new CumulativeDecayStrategy(TestUtil.clock(2019, 1, 1, 0, 0, 0)))
        .build();
  }
}
