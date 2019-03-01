package wtf.benedict.kitchen.data;

import java.util.Arrays;

import lombok.AllArgsConstructor;

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
