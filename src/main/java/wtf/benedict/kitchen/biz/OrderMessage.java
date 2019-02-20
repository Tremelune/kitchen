package wtf.benedict.kitchen.biz;

import lombok.Builder;
import lombok.Data;

/** Java DTO for orders coming from an event, request, or JSON file. */
@Builder
@Data
public class OrderMessage {
  private String name;
  private String temp;
  private int shelfLife; // In seconds.
  private double decayRate;
}
