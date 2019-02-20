package wtf.benedict.kitchen.test;

import static java.time.ZoneOffset.UTC;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

public class TestUtil {
  public static Clock clock(int year, int month, int day, int hour, int min, int sec) {
    return Clock.fixed(instant(year, month, day, hour, min, sec), UTC);
  }

  public static Instant instant(int year, int month, int day, int hour, int min, int sec) {
    return LocalDateTime.of(year, month, day, hour, min, sec).toInstant(UTC);
  }


  // This exists to avoid IntelliJ's warnings about Arrays.asList() for a single object. We could
  // disable that inspection, but then everyone would have to...
  @SafeVarargs
  public static <T> List<T> asList(T... objects) {
    return Arrays.asList(objects);
  }


  public static void assertSize(int expected, Collection collection) {
    if (collection == null || expected < 0) {
      Assert.fail();
    }

    Assert.assertEquals(expected, collection.size());
  }

  public static void assertSize(int expected, Map map) {
    if (map == null || expected < 0) {
      Assert.fail();
    }

    Assert.assertEquals(expected, map.size());
  }

  public static void assertSize(int expected, Object[] array) {
    if (array == null || expected < 0) {
      Assert.fail();
    }

    Assert.assertEquals(expected, array.length);
  }


  private TestUtil() {
  }
}
