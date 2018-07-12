// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.io.export;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.cache.CacheEntry;
import org.openstreetmap.josm.data.cache.CacheEntryAttributes;
import org.openstreetmap.josm.data.cache.ICachedLoaderListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.cache.CacheUtils;
import org.openstreetmap.josm.plugins.streetside.cache.StreetsideCache;

/**
 * This is the thread that downloads one of the images that are going to be
 * exported and writes them in a {@link ArrayBlockingQueue}.
 *
 * @author nokutu
 * @see StreetsideExportManager
 * @see StreetsideExportWriterThread
 */
public class StreetsideExportDownloadThread extends Thread implements
    ICachedLoaderListener {

  final static Logger logger = Logger.getLogger(StreetsideExportDownloadThread.class);

  private final ArrayBlockingQueue<BufferedImage> queue;
  private final ArrayBlockingQueue<StreetsideAbstractImage> queueImages;

  private final StreetsideImage image;

  /**
   * Main constructor.
   *
   * @param image
   *          Image to be downloaded.
   * @param queue
   *          Queue of {@link BufferedImage} objects for the
   *          {@link StreetsideExportWriterThread}.
   * @param queueImages
   *          Queue of {@link StreetsideAbstractImage} objects for the
   *          {@link StreetsideExportWriterThread}.
   */
  public StreetsideExportDownloadThread(StreetsideImage image,
      ArrayBlockingQueue<BufferedImage> queue,
      ArrayBlockingQueue<StreetsideAbstractImage> queueImages) {
    this.queue = queue;
    this.image = image;
    this.queueImages = queueImages;
  }

  @Override
  public void run() {
    // use "thumbnail" type here so that the tiles are not exported
    CacheUtils.submit(image.getId(), StreetsideCache.Type.THUMBNAIL, this);
  }

  @Override
  public synchronized void loadingFinished(CacheEntry data,
      CacheEntryAttributes attributes, LoadResult result) {
    try {
      synchronized (StreetsideExportDownloadThread.class) {
        queue
            .put(ImageIO.read(new ByteArrayInputStream(data.getContent())));
        queueImages.put(image);
      }
    } catch (InterruptedException | IOException e) {
      logger.error(e);
    }
  }
}
