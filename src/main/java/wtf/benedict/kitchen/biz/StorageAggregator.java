package wtf.benedict.kitchen.biz;

import static java.util.stream.Collectors.toList;
import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.val;
import wtf.benedict.kitchen.biz.DriverDepot.Delivery;

public class StorageAggregator {
  StorageState getState(Storage storage, Map<Long, Delivery> orderIdToDelivery) {
    val hotEntries = toEntries(storage, HOT);
    val coldEntries = toEntries(storage, COLD);
    val frozenEntries = toEntries(storage, FROZEN);

    val overflowEntries = new ArrayList<Entry>();
    for (OrderQueue queue : storage.overflowShelf.queues.values()) {
      val entries = toEntries(queue.freshOrders.values());
      overflowEntries.addAll(entries);
    }

    return StorageState.builder()
        .hotEntries(hotEntries)
        .coldEntries(coldEntries)
        .frozenEntries(frozenEntries)
        .overflowEntries(overflowEntries)
        .orderIdToDelivery(orderIdToDelivery)
        .build();
  }


  private static List<Entry> toEntries(Storage storage, Temperature temp) {
    return toEntries(storage.tempToShelf.get(temp).queue.freshOrders.values());
  }


  private static List<Entry> toEntries(Collection<Order> orders) {
    return orders.stream()
        .map(StorageAggregator::toEntry)
        .collect(toList());
  }


  private static Entry toEntry(Order order) {
    return Entry.builder()
        .name(order.getName())
        .temp(order.getTemp())
        .remainingShelfLife(order.calculateRemainingShelfLife())
        .build();
  }



  /** This is part of the public API. Changes here might break it! */
  @Builder
  @Getter
  public static class StorageState {
    private List<Entry> hotEntries;
    private List<Entry> coldEntries;
    private List<Entry> frozenEntries;
    private List<Entry> overflowEntries;
    private Map<Long, Delivery> orderIdToDelivery;
  }


  @Builder
  @Getter
  public static class Entry {
    private String name;
    private Temperature temp;
    private long remainingShelfLife;
  }
}
