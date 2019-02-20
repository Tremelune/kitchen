package wtf.benedict.kitchen.biz;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;
import static wtf.benedict.kitchen.test.TestUtil.assertSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.biz.DriverDepot.Pickup;
import wtf.benedict.kitchen.biz.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.test.TestUtil;

public class StorageAggregatorTest {
  private StorageAggregator storageAggregator;


  @Before
  public void setUp() {
    storageAggregator = new StorageAggregator();
  }


  @Test
  public void getState() throws Exception {
    Storage storage = new Storage((id, order) -> {}, new ArrayList<>());

    val a = newOrder(10, "a", HOT, 100);
    val b = newOrder(11, "b", COLD, 50);
    val c = newOrder(12, "c", FROZEN, 200);

    storage.put(a);
    storage.put(b);
    storage.put(c);

    val orderIdToDelivery = new HashMap<Long, Pickup>() {{
      put(10L, new Pickup(a, Instant.now().plusSeconds(100)));
    }};

    val state = storageAggregator.getState(storage, orderIdToDelivery, new ArrayList<>());

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
  public void getState_overflow() throws Exception {
    Storage storage = new Storage((id, order) -> {}, new ArrayList<>());

    overFlowOrders(storage, 10, HOT);
    overFlowOrders(storage, 20, COLD);
    overFlowOrders(storage, 30, FROZEN);

    val state = storageAggregator.getState(storage, emptyMap(), new ArrayList<>());

    // Order isn't guaranteed, so just see if they're there...
    assertEquals(3, state.getOverflowEntries().size());
    assertNotNull(state.getOverflowEntries().stream().filter((entry) -> entry.getName().equals("10")).findFirst().get());
    assertNotNull(state.getOverflowEntries().stream().filter((entry) -> entry.getName().equals("20")).findFirst().get());
    assertNotNull(state.getOverflowEntries().stream().filter((entry) -> entry.getName().equals("30")).findFirst().get());
  }


  // Overflow every shelf by one.
  private void overFlowOrders(Storage storage, int baseId, Temperature temp) throws StaleOrderException {
    for (int i = 0; i < 16; i++) {
      val id = baseId + i;
      val order = newOrder(id, String.valueOf(id), temp, 100);
      storage.put(order);
    }
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
