package wtf.benedict.kitchen.biz;

import java.time.Instant;
import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Order {
  @AllArgsConstructor
  public enum Temperature {
    HOT("hot"),
    COLD("cold"),
    FROZEN("frozen");

    public static Temperature fromValue(String value) {
      return Arrays.stream(values())
          .filter((temp) -> temp.value.equals(value))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("No Temperature found for: " + value));
    }

    private final String value;
  }


  private long id;
  private String name;
  private Temperature temp;
  private int shelfLife; // In seconds.
  private double decayRate;
  private Instant received;
}
