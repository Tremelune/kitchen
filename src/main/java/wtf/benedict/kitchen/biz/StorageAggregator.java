package wtf.benedict.kitchen.biz;

import static java.util.stream.Collectors.toList;
import static wtf.benedict.kitchen.biz.Temperature.COLD;
import static wtf.benedict.kitchen.biz.Temperature.FROZEN;
import static wtf.benedict.kitchen.biz.Temperature.HOT;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import wtf.benedict.kitchen.biz.DriverDepot.Pickup;

public class StorageAggregator {
  StorageState getState(Storage storage, Map<Long, Pickup> orderIdToDelivery) {
    val hotEntries = toEntries(storage, HOT);
    val coldEntries = toEntries(storage, COLD);
    val frozenEntries = toEntries(storage, FROZEN);

    val overflowEntries = new ArrayList<Entry>();
    for (OrderQueue queue : storage.overflowShelf.queues.values()) {
      val entries = toEntries(queue.freshOrders.values());
      overflowEntries.addAll(entries);
    }

    val pickups = orderIdToDelivery.values().stream()
        .map(StorageAggregator::toScheduledPickup)
        .sorted(newPickupComparator())
        .collect(toList());

    return StorageState.builder()
        .hotEntries(hotEntries)
        .coldEntries(coldEntries)
        .frozenEntries(frozenEntries)
        .overflowEntries(overflowEntries)
        .pickups(pickups)
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


  private static ScheduledPickup toScheduledPickup(Pickup pickup) {
    val duration = Duration.between(Instant.now(), pickup.getTime());
    val seconds = Math.max(0, duration.getSeconds());
    return new ScheduledPickup(pickup.getOrder().getName(), seconds);
  }


  // In order of pickup time, soonest first.
  private static Comparator<ScheduledPickup> newPickupComparator() {
    return Comparator.comparingLong(ScheduledPickup::getSecondsUntilPickup);
  }


  /** These are part of the public API. Changes here might break it! */
  @Builder
  @Data
  public static class StorageState {
    private List<Entry> hotEntries;
    private List<Entry> coldEntries;
    private List<Entry> frozenEntries;
    private List<Entry> overflowEntries;
    private List<ScheduledPickup> pickups;
  }


  @Builder
  @Data
  public static class Entry {
    private String name;
    private Temperature temp;
    private long remainingShelfLife;
  }


  @AllArgsConstructor
  @Data
  public static class ScheduledPickup {
    private String orderName;
    private long secondsUntilPickup;
  }
}
