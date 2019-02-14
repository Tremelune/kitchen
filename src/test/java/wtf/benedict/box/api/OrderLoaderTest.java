package wtf.benedict.box.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lombok.val;

public class OrderLoaderTest {
  @Test
  public void next() {
    val underTest = new OrderLoader();

    val order = underTest.next();

    assertEquals("Banana Split", order.getName());
//    assertEquals(FROZEN, order.getTemperature()); // TODO Why doesn't this work?
    assertEquals(20, order.getShelfLife());
    assertEquals(0.63, order.getDecayRate(), 0);
  }
}
