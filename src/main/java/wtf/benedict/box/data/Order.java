package wtf.benedict.box.data;

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


  private String name;
  private Temperature temperature;
  private int shelfLife; // In seconds.
  private float decayRate;
}
