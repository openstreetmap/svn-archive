// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.mapillary.actions;

import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.plugins.mapillary.MapillaryAbstractImage;
import org.openstreetmap.josm.plugins.mapillary.MapillaryData;
import org.openstreetmap.josm.plugins.mapillary.MapillaryDataListener;
import org.openstreetmap.josm.plugins.mapillary.MapillaryImage;
import org.openstreetmap.josm.plugins.mapillary.MapillaryLayer;
import org.openstreetmap.josm.plugins.mapillary.cache.CacheUtils;
import org.openstreetmap.josm.plugins.mapillary.gui.MapillaryMainDialog;

/**
 * Thread containing the walk process.
 *
 * @author nokutu
 *
 */
public class WalkThread extends Thread implements MapillaryDataListener {
  private final int interval;
  private final MapillaryData data;
  private boolean end = false;
  private final boolean waitForFullQuality;
  private final boolean followSelected;
  private final boolean goForward;
  private BufferedImage lastImage;
  private volatile boolean paused = false;

  /**
   * Main constructor.
   *
   * @param interval
   *          How often the images switch.
   * @param waitForPicture
   *          If it must wait for the full resolution picture or just the
   *          thumbnail.
   * @param followSelected
   *          Zoom to each image that is selected.
   * @param goForward
   *          true to go forward; false to go backwards.
   */
  public WalkThread(int interval, boolean waitForPicture,
      boolean followSelected, boolean goForward) {
    this.interval = interval;
    this.waitForFullQuality = waitForPicture;
    this.followSelected = followSelected;
    this.goForward = goForward;
    this.data = MapillaryLayer.getInstance().getData();
    this.data.addListener(this);
  }

  @Override
  public void run() {
    try {
      while (!this.end && this.data.getSelectedImage().next() != null) {
        MapillaryAbstractImage image = this.data.getSelectedImage();
        if (image instanceof MapillaryImage) {
          // Predownload next 10 thumbnails.
          for (int i = 0; i < 10; i++) {
            if (image.next() == null)
              break;
            image = image.next();
            CacheUtils.downloadPicture((MapillaryImage) image,
                CacheUtils.PICTURE.THUMBNAIL);
          }
          if (this.waitForFullQuality)
            // Start downloading 3 next full images.
            for (int i = 0; i < 3; i++) {
              if (image.next() == null)
                break;
              image = image.next();
              CacheUtils.downloadPicture((MapillaryImage) image,
                  CacheUtils.PICTURE.FULL_IMAGE);
            }
        }
        try {
          synchronized (this) {
            // Waits for full quality picture.
            if (this.waitForFullQuality && image instanceof MapillaryImage) {
              while (MapillaryMainDialog.getInstance().mapillaryImageDisplay
                  .getImage() == this.lastImage
                  || MapillaryMainDialog.getInstance().mapillaryImageDisplay
                      .getImage() == null
                  || MapillaryMainDialog.getInstance().mapillaryImageDisplay
                      .getImage().getWidth() < 2048)
                wait(100);
            }
            // Waits for thumbnail.
            else {
              while (MapillaryMainDialog.getInstance().mapillaryImageDisplay
                  .getImage() == this.lastImage
                  || MapillaryMainDialog.getInstance().mapillaryImageDisplay
                      .getImage() == null
                  || MapillaryMainDialog.getInstance().mapillaryImageDisplay
                      .getImage().getWidth() < 320)
                wait(100);
            }
            while (this.paused)
              wait(100);
            wait(this.interval);
            while (this.paused)
              wait(100);
          }
          this.lastImage = MapillaryMainDialog.getInstance().mapillaryImageDisplay
              .getImage();
          synchronized (this) {
            if (this.goForward)
              this.data.selectNext(this.followSelected);
            else
              this.data.selectPrevious(this.followSelected);
          }
        } catch (InterruptedException e) {
          return;
        }
      }
    } catch (NullPointerException e) {
      return;
    }
    end();
  }

  @Override
  public synchronized void interrupt() {
    super.interrupt();
  }

  @Override
  public void imagesAdded() {
    // Nothing
  }

  @Override
  public void selectedImageChanged(MapillaryAbstractImage oldImage,
      MapillaryAbstractImage newImage) {
    if (newImage != oldImage.next()) {
      end();
      interrupt();
    }
  }

  /**
   * Continues with the execution if paused.
   */
  public void play() {
    this.paused = false;
  }

  /**
   * Pauses the execution.
   */
  public void pause() {
    this.paused = true;
  }

  /**
   * Stops the execution.
   */
  public void stopWalk() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          stopWalk();
        }
      });
    } else {
      end();
      this.interrupt();
    }
  }

  /**
   * Called when the walk stops by itself of forcefully.
   */
  public void end() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          end();
        }
      });
    } else {
      this.end = true;
      this.data.removeListener(this);
      MapillaryMainDialog.getInstance()
          .setMode(MapillaryMainDialog.MODE.NORMAL);
    }
  }
}
