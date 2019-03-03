package wtf.benedict.kitchen.biz.kitchen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import wtf.benedict.kitchen.biz.delivery.DriverDepot;
import wtf.benedict.kitchen.biz.kitchen.OverflowShelf.StaleOrderException;
import wtf.benedict.kitchen.data.Order;

@RunWith(MockitoJUnitRunner.class)
public class KitchenTest {
  private final ExecutorService executorService = new TestExecutor();

  @Mock
  private DriverDepot driverDepot;
  @Mock
  private Shelf shelf;
  @Mock
  private Trash trash;

  @Mock
  private Order order;

  private Kitchen underTest;


  @Before
  public void setUp() {
    underTest = new Kitchen(driverDepot, executorService, shelf, trash);
  }


  @Test
  public void receiveOrder() throws Exception {
    underTest.receiveOrder(order);
    verify(shelf, times(1)).put(eq(order));
    verify(driverDepot, times(1)).schedulePickup(any(), eq(order));
  }


  @Test
  public void receiveOrder_trash() throws Exception {
    doThrow(new StaleOrderException(order)).when(shelf).put(eq(order));
    underTest.receiveOrder(order);
    verify(trash, times(1)).add(eq(order));
  }


  // Allows for immediate execution.
  private static class TestExecutor extends DummyExecutor {
    @Override
    public Future<?> submit(Runnable task) {
      task.run();
      return null;
    };
  }


  // Keeps the dummy implementation from the test logic.
  private static class DummyExecutor implements ExecutorService {
    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
      return null;
    }

    @Override
    public boolean isShutdown() {
      return false;
    }

    @Override
    public boolean isTerminated() {
      return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
      return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
      return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
      return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
      return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return null;
    }

    @Override
    public void execute(Runnable command) {

    }
  }
}