package wtf.benedict.kitchen.biz;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderMessage {
  private String name;
  private String temp;
  private int shelfLife; // In seconds.
  private double decayRate;
}
