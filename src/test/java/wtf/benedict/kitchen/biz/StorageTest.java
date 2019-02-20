package wtf.benedict.kitchen.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import org.junit.Test;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

public class StorageTest {
  @Test
  public void orderShouldBeFoundById() throws Exception {
    val underTest = new Storage((id, order) -> {}, new Trash(null));

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
}
