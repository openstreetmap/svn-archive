package org.openstreetmap.josm.plugins.streetside.cubemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;

public class TileDownloadingTaskTest {

  @SuppressWarnings("static-method")
  @Ignore
  @Test
  public final void testCall() {
    ExecutorService pool = Executors.newFixedThreadPool(1);
    List<Callable<List<String>>> tasks = new ArrayList<>(1);
      tasks.add(new TileDownloadingTask("2202112030033001233"));
      try {
        List<Future<List<String>>> results = pool.invokeAll(tasks);
        assertEquals(results.get(0),"2202112030033001233");
      } catch (InterruptedException e) {
        e.printStackTrace();
        fail("Test threw an exception.");
      }
  }
}