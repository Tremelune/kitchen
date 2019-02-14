package wtf.benedict.kitchen.test;

import static java.time.ZoneOffset.UTC;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;

// TODO Prune unused.
public class TestUtil {
  private TestUtil() {
  }


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


  public static int randomId() {
    // Primary keys are longs, but it's possible to generated too-large a long for an ID, so we
    // generate a random int. We also can't have negatives, sooo...
    return Math.abs(new Random().nextInt());
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

  public static void assertEmpty(Collection collection) {
    Assert.assertTrue(isEmpty(collection));
  }

  public static void assertNotEmpty(Collection collection) {
    Assert.assertFalse(isEmpty(collection));
  }

  public static void assertEmpty(Map map) {
    Assert.assertTrue(map == null || isEmpty(map.keySet()));
  }

  public static void assertNotEmpty(Map map) {
    Assert.assertNotNull(map);
    Assert.assertFalse(isEmpty(map.keySet()));
  }

  public static void assertJsonEquals(String expected, String actual) {
    expected = stripWhitespace(expected);
    actual = stripWhitespace(actual);
    assertEquals(expected, actual);
  }

  public static boolean jsonEquals(String expected, String actual) {
    expected = stripWhitespace(expected);
    actual = stripWhitespace(actual);
    return expected.equals(actual);
  }

  private static String stripWhitespace(String s) {
    return s.replaceAll("\\s+","");
  }
}
