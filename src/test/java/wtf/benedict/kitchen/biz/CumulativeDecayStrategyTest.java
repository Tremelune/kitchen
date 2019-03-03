package wtf.benedict.kitchen.biz;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import wtf.benedict.kitchen.test.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CumulativeDecayStrategyTest {
  @Mock
  private Clock clock;

  private CumulativeDecayStrategy undertest;


  @Before
  public void setUp() {
   undertest = new CumulativeDecayStrategy(clock);
  }


  @Test
  public void calculateRemainingShelfLife() {
    assertEquals(100, undertest.calculateRemainingShelfLife(1, 100));

    // Add actual rate change
    when(clock.instant()).thenReturn(TestUtil.instant(2019, 1, 1, 0, 0, 0));
    undertest.changeDecayRate(2);
    assertEquals(50, undertest.calculateRemainingShelfLife(1, 100));
    assertEquals(25, undertest.calculateRemainingShelfLife(2, 100)); // Test initial rate changes

    // Change rate, first lasted 1s.
    when(clock.instant()).thenReturn(TestUtil.instant(2019, 1, 1, 0, 0, 1)); // 1s
    undertest.changeDecayRate(3);
    assertEquals(33, undertest.calculateRemainingShelfLife(1, 100)); // (100 - 2) / 3

    // Change rate, previous lasted 2s.
    when(clock.instant()).thenReturn(TestUtil.instant(2019, 1, 1, 0, 0, 3)); // 2s
    undertest.changeDecayRate(1);
    assertEquals(92, undertest.calculateRemainingShelfLife(1, 100)); // (100 - 2 - 2 * 3) / 1
  }


  @Test
  public void calculateRemainingShelfLifeAt() {
    assertEquals(100, undertest.calculateRemainingShelfLifeAt(1, 100, 1));
    assertEquals(50, undertest.calculateRemainingShelfLifeAt(1, 100, 2));
    assertEquals(25, undertest.calculateRemainingShelfLifeAt(2, 100, 2));
    assertEquals(33, undertest.calculateRemainingShelfLifeAt(1, 100, 3)); // Rounding
  }


  @Test
  public void calculateDecayDepletion() {
    val start = TestUtil.instant(2019, 1, 1, 0, 0, 0);
    val end = TestUtil.instant(2019, 1, 1, 0, 0, 30);
    assertEquals(30, (long) CumulativeDecayStrategy.calculateDecayDepletion(start, end, 1));
    assertEquals(60, (long) CumulativeDecayStrategy.calculateDecayDepletion(start, end, 2));
  }


  @Test(expected = IllegalArgumentException.class)
  public void calculateDecayDepletion_timesArrow() {
    val start = TestUtil.instant(2019, 1, 1, 0, 0, 30);
    val end = TestUtil.instant(2019, 1, 1, 0, 0, 0);
    assertEquals(30, CumulativeDecayStrategy.calculateDecayDepletion(start, end, 1));
  }


  @Test
  public void calculateRemainingShelfLife_internal() {
    long initialShelfLife = 100;
    long totalDuration = 10;
    double decayDepletion = 5;
    double currentDecayRate = 5;

    long remainingShelfLife = CumulativeDecayStrategy.calculateRemainingShelfLife(
        initialShelfLife, totalDuration, decayDepletion, currentDecayRate);

    assertEquals(17, remainingShelfLife); // (100 - 10 - 5) / 5
  }


  @Test
  public void calculateRemainingShelfLife_rounding() {
    long initialShelfLife = 100;
    long totalDuration = 1;
    double decayDepletion = .5;
    double currentDecayRate = 1;

    long remainingShelfLife = CumulativeDecayStrategy.calculateRemainingShelfLife(
        initialShelfLife, totalDuration, decayDepletion, currentDecayRate);

    assertEquals(99, remainingShelfLife); // 98.5
  }


  @Test
  public void calculateRemainingShelfLife_negatives() {
    long initialShelfLife = 100;
    long totalDuration = 1;
    double decayDepletion = 1;
    double currentDecayRate = -1; // Impossible...

    long remainingShelfLife = CumulativeDecayStrategy.calculateRemainingShelfLife(
        initialShelfLife, totalDuration, decayDepletion, currentDecayRate);

    assertEquals(0, remainingShelfLife); // -98
  }
}
