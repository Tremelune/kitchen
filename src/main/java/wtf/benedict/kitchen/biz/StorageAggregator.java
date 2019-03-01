package wtf.benedict.kitchen.biz;

import static java.util.stream.Collectors.toList;
import static wtf.benedict.kitchen.data.Temperature.COLD;
import static wtf.benedict.kitchen.data.Temperature.FROZEN;
import static wtf.benedict.kitchen.data.Temperature.HOT;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import wtf.benedict.kitchen.data.storage.DriverStorage;
import wtf.benedict.kitchen.data.storage.DriverStorage.Pickup;
import wtf.benedict.kitchen.data.Order;
import wtf.benedict.kitchen.data.RemainingShelfLifeComparator;
import wtf.benedict.kitchen.data.storage.ShelfStorage;
import wtf.benedict.kitchen.data.Temperature;
import wtf.benedict.kitchen.data.storage.TrashStorage;

/**
 * Aggregates the state of the system and bundles it up for serialization into a response.
 *
 * I could see separating the aggregation from the conversion to an API response, but so far this
 * seems straightforward enough not to worry about it yet.
 */
@AllArgsConstructor
public class StorageAggregator {
  private final DriverStorage driverStorage;
  private final ShelfStorage overflowStorage;
  private final ShelfStorage shelfStorage;
  private final TrashStorage trashStorage;


  /**
   * @return State of the whole system, including waiting orders, shelves, trash, and delivery
   * drivers.
   */
  public StorageState getState() {
    val hotEntries = toEntries(shelfStorage.getAll(HOT));
    val coldEntries = toEntries(shelfStorage.getAll(COLD));
    val frozenEntries = toEntries(shelfStorage.getAll(FROZEN));
    val overflowEntries = toEntries(overflowStorage.getAll());

    val pickups = driverStorage.getAll().stream()
        .map(StorageAggregator::toScheduledPickup)
        .sorted(newPickupComparator())
        .collect(toList());

    val trashedEntries = trashStorage.getAll().stream()
        .map(StorageAggregator::toEntry)
        .collect(toList());

    return StorageState.builder()
        .hotEntries(hotEntries)
        .coldEntries(coldEntries)
        .frozenEntries(frozenEntries)
        .overflowEntries(overflowEntries)
        .pickups(pickups)
        .trashedEntries(trashedEntries)
        .build();
  }


  private static List<Entry> toEntries(Collection<Order> orders) {
    return orders.stream()
        .sorted(RemainingShelfLifeComparator.INSTANCE)
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
    private List<Entry> trashedEntries;
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
