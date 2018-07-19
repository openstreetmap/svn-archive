// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.io.export;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;

/**
 * Export main thread. Exportation works by creating a
 * {@link StreetsideExportWriterThread} and several
 * {@link StreetsideExportDownloadThread}. The second ones download every single
 * image that is going to be exported and stores them in an
 * {@link ArrayBlockingQueue}. Then it is picked by the first one and written on
 * the selected folder. Each image will be named by its key.
 *
 * @author nokutu
 * @see StreetsideExportWriterThread
 * @see StreetsideExportDownloadThread
 */
public class StreetsideExportManager extends PleaseWaitRunnable {

  final static Logger logger = Logger.getLogger(StreetsideExportManager.class);

  private final ArrayBlockingQueue<BufferedImage> queue = new ArrayBlockingQueue<>(10);
  private final ArrayBlockingQueue<StreetsideAbstractImage> queueImages = new ArrayBlockingQueue<>(10);

  private int amount;
  private Set<StreetsideAbstractImage> images;
  private String path;

  private Thread writer;
  private ThreadPoolExecutor ex;

  /**
   * Main constructor.
   *
   * @param images Set of {@link StreetsideAbstractImage} objects to be exported.
   * @param path Export path.
   */
  public StreetsideExportManager(Set<StreetsideAbstractImage> images, String path) {
    super(
      tr("Downloading") + "…",
      new PleaseWaitProgressMonitor(tr("Exporting Streetside Images")),
      true
    );
    this.images = images == null ? new HashSet<>() : images;
    this.path = path;
    amount = this.images.size();
  }

  @Override
  protected void cancel() {
    writer.interrupt();
    ex.shutdown();
  }

  @Override
  protected void realRun() throws IOException {
    // Starts a writer thread in order to write the pictures on the disk.
    writer = new StreetsideExportWriterThread(path, queue,
        queueImages, amount, getProgressMonitor());
    writer.start();
    if (path == null) {
      try {
        writer.join();
      } catch (InterruptedException e) {
        logger.error(e);
      }
      return;
    }
    ex = new ThreadPoolExecutor(20, 35, 25, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(10));
    for (StreetsideAbstractImage image : images) {
      if (image instanceof StreetsideImage) {
        try {
          ex.execute(new StreetsideExportDownloadThread(
              (StreetsideImage) image, queue, queueImages));
        } catch (Exception e) {
          logger.error(e);
        }
      }
      try {
        // If the queue is full, waits for it to have more space
        // available before executing anything else.
        while (ex.getQueue().remainingCapacity() == 0) {
          Thread.sleep(100);
        }
      } catch (Exception e) {
        logger.error(e);
      }
    }
    try {
      writer.join();
    } catch (InterruptedException e) {
      logger.error(e);
    }
  }

  @Override
  protected void finish() {
  }
}
