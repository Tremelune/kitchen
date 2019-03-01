package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;
import static wtf.benedict.kitchen.test.TestUtil.asList;
import static wtf.benedict.kitchen.test.TestUtil.assertSize;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import net.jodah.expiringmap.ExpirationListener;
import wtf.benedict.kitchen.biz.DriverStorage.Pickup;
import wtf.benedict.kitchen.test.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class StorageAggregatorTest {
  @Mock
  private DriverStorage driverStorage;
  @Mock
  private ExpirationListener<Long, Order> expirationListener;
  @Mock
  private ShelfStorage overflowStorage;

  private ShelfStorage shelfStorage;
  private StorageAggregator underTest;


  @Before
  public void setUp() {
    shelfStorage = new ShelfStorage(10, 1, expirationListener);
    val trashStorage = new TrashStorage();
    underTest = new StorageAggregator(driverStorage, overflowStorage, shelfStorage, trashStorage);
  }


  @Test
  public void getState() throws Exception {
    val a = newOrder(10, "a", HOT, 100);
    val b = newOrder(11, "b", COLD, 50);
    val c = newOrder(12, "c", FROZEN, 200);

    shelfStorage.put(a);
    shelfStorage.put(b);
    shelfStorage.put(c);

    val pickup = new Pickup(a, Instant.now().plusSeconds(100));
    when(driverStorage.getAll()).thenReturn(asList(pickup));

    val state = underTest.getState();

    assertEquals(1, state.getHotEntries().size());
    assertEquals("a", state.getHotEntries().get(0).getName());
    assertEquals(HOT, state.getHotEntries().get(0).getTemp());
    assertEquals(100, state.getHotEntries().get(0).getRemainingShelfLife());

    assertEquals(1, state.getColdEntries().size());
    assertEquals("b", state.getColdEntries().get(0).getName());
    assertEquals(COLD, state.getColdEntries().get(0).getTemp());
    assertEquals(50, state.getColdEntries().get(0).getRemainingShelfLife());

    assertEquals(1, state.getFrozenEntries().size());
    assertEquals("c", state.getFrozenEntries().get(0).getName());
    assertEquals(FROZEN, state.getFrozenEntries().get(0).getTemp());
    assertEquals(200, state.getFrozenEntries().get(0).getRemainingShelfLife());

    assertSize(1, state.getPickups());
    assertEquals("a", state.getPickups().get(0).getOrderName());
    assertTrue(state.getPickups().get(0).getSecondsUntilPickup() >= 99); // Time passing slush...
  }


  @Test
  public void getState_overflow() {
    val a = newOrder(10, "a", HOT, 100);
    val b = newOrder(20, "b", COLD, 100);
    val c = newOrder(30, "c", FROZEN, 100);
    when(overflowStorage.getAll()).thenReturn(asList(a, b, c));

    val state = underTest.getState();

    // Order isn't guaranteed, so just see if they're there...
    assertEquals(3, state.getOverflowEntries().size());
    assertNotNull(state.getOverflowEntries().stream().filter((entry) -> entry.getName().equals("a")).findFirst().get());
    assertNotNull(state.getOverflowEntries().stream().filter((entry) -> entry.getName().equals("b")).findFirst().get());
    assertNotNull(state.getOverflowEntries().stream().filter((entry) -> entry.getName().equals("c")).findFirst().get());
  }


  private static Order newOrder(long id, String name, Temperature temp, int initialShelfLife) {
    return new Order.Builder()
        .id(id)
        .name(name)
        .temp(temp)
        .baseDecayRate(1)
        .initialShelfLife(initialShelfLife)
        .decayStrategy(new CumulativeDecayStrategy(TestUtil.clock(2019, 1, 1, 0, 0, 0)))
        .build();
  }
}
