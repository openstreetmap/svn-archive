// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.actions;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideData;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.StreetsidePlugin;
import org.openstreetmap.josm.plugins.streetside.cache.CacheUtils;
import org.openstreetmap.josm.plugins.streetside.cache.StreetsideCache;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

import org.openstreetmap.josm.plugins.streetside.StreetsideImage;

/**
 * Thread containing the walk process.
 *
 * @author nokutu
 */
public class WalkThread extends Thread implements StreetsideDataListener {
  private final int interval;
  private final StreetsideData data;
  private boolean end;
  private final boolean waitForFullQuality;
  private final boolean followSelected;
  private final boolean goForward;
  private volatile boolean paused;

  /**
   * Main constructor.
   *
   * @param interval How often the images switch.
   * @param waitForPicture If it must wait for the full resolution picture or just the
   * thumbnail.
   * @param followSelected Zoom to each image that is selected.
   * @param goForward true to go forward; false to go backwards.
   */
  public WalkThread(int interval, boolean waitForPicture,
                    boolean followSelected, boolean goForward) {
    this.interval = interval;
    waitForFullQuality = waitForPicture;
    this.followSelected = followSelected;
    this.goForward = goForward;
    data = StreetsideLayer.getInstance().getData();
    data.addListener(this);
  }

  @Override
  public void run() {
    try {
      StreetsideAbstractImage curSelection;
      StreetsideImage curImage;
      while (
          !end &&
          (curSelection = data.getSelectedImage().next()) != null &&
          (curImage = curSelection instanceof StreetsideImage ? (StreetsideImage) curSelection : null) != null
      ) {
        // Predownload next 10 thumbnails.
        preDownloadImages(curImage, 10, CacheUtils.PICTURE.THUMBNAIL, goForward);
        if (waitForFullQuality) {
          // Start downloading 3 next full images.
          preDownloadImages(curImage, 3, CacheUtils.PICTURE.FULL_IMAGE, goForward);
        }
        try {
          // Wait for picture for 1 minute.
          final StreetsideCache cache = new StreetsideCache(curImage.getId(), waitForFullQuality ? StreetsideCache.Type.FULL_IMAGE : StreetsideCache.Type.THUMBNAIL);
          int limit = 240; // 240 * 250 = 60000 ms
          while (cache.get() == null) {
            Thread.sleep(250);
            if (limit-- < 0) {
              new Notification(I18n.tr("Walk mode: Waiting for next image takes too long! Exiting walk mode…"))
                  .setIcon(StreetsidePlugin.LOGO.get())
                  .show();
              end();
              return;
            }
          }
          while (paused) {
            Thread.sleep(100);
          }
          Thread.sleep(interval);
          while (paused) {
            Thread.sleep(100);
          }
          if (goForward) {
            data.selectNext(followSelected);
          } else {
            data.selectPrevious(followSelected);
          }
        } catch (InterruptedException e) {
          end();
          return;
        }

      }
    } catch (NullPointerException e) {
      Logging.warn(e);
      end();
      // TODO: Avoid NPEs instead of waiting until they are thrown and then catching them
      return;
    }
    end();
  }

  /**
   * Downloads n images into the cache beginning from the supplied start-image (including the start-image itself).
   *
   * @param startImage the image to start with (this and the next n-1 images in the same sequence are downloaded)
   * @param n the number of images to download
   * @param type the quality of the image (full or thumbnail)
   * @param goForward true if the next images, false if the previous ones should be downloaded
   */
  private static void preDownloadImages(StreetsideImage startImage, int n, CacheUtils.PICTURE type, final boolean goForward) {
    if (n >= 1 && startImage != null) {
      CacheUtils.downloadPicture(startImage, type);
      final StreetsideAbstractImage nextImg = goForward ? startImage.next() : startImage.previous();
      if (nextImg instanceof StreetsideImage && n >= 2) {
        preDownloadImages((StreetsideImage) nextImg, n - 1, type, goForward);
      }
    }
  }

  @Override
  public void imagesAdded() {
    // Nothing
  }

  @Override
  public void selectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {
    if (newImage != oldImage.next()) {
      end();
      interrupt();
    }
  }

  /**
   * Continues with the execution if paused.
   */
  public void play() {
    paused = false;
  }

  /**
   * Pauses the execution.
   */
  public void pause() {
    paused = true;
  }

  /**
   * Stops the execution.
   */
  public void stopWalk() {
    if (SwingUtilities.isEventDispatchThread()) {
      end();
      interrupt();
    } else {
      SwingUtilities.invokeLater(this::stopWalk);
    }
  }

  /**
   * Called when the walk stops by itself of forcefully.
   */
  private void end() {
    if (SwingUtilities.isEventDispatchThread()) {
      end = true;
      data.removeListener(this);
      StreetsideMainDialog.getInstance().setMode(StreetsideMainDialog.MODE.NORMAL);
    } else {
      SwingUtilities.invokeLater(this::end);
    }
  }
}
