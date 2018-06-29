// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.io.download;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.utils.PluginState;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideUtils;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

public class StreetsideSquareDownloadRunnable implements Runnable {

  final static Logger logger = Logger.getLogger(StreetsideSquareDownloadRunnable.class);

  private final Bounds bounds;

  /**
   * Main constructor.
   *
   * @param bounds the bounds of the area that should be downloaded
   *
   */
  public StreetsideSquareDownloadRunnable(Bounds bounds) {
    this.bounds = bounds;
  }

  @Override
  public void run() {
    PluginState.startDownload();
    StreetsideUtils.updateHelpText();

    // Download basic sequence data synchronously
    new SequenceDownloadRunnable(StreetsideLayer.getInstance().getData(), bounds).run();

    if (Thread.interrupted()) {
      return;
    }

    // TODO: Revamp image details for Streetside RRH
    // Asynchronously load the rest of the image details
    Thread imgDetailsThread = new Thread(new ImageDetailsDownloadRunnable(StreetsideLayer.getInstance().getData(), bounds));
    imgDetailsThread.start();

    // TODO: Do we support detections? RRH
    /*Thread detectionsThread = new Thread(new DetectionsDownloadRunnable(StreetsideLayer.getInstance().getData(), bounds));
    detectionsThread.start();*/

    try {
      imgDetailsThread.join();
      //detectionsThread.join();
    } catch (InterruptedException e) {
      logger.warn(I18n.tr("Streetside download interrupted (probably because of closing the layer).", e));
      Thread.currentThread().interrupt();
    } finally {
      PluginState.finishDownload();
    }

    StreetsideUtils.updateHelpText();
    StreetsideLayer.invalidateInstance();
    //StreetsideFilterDialog.getInstance().refresh();
    StreetsideMainDialog.getInstance().updateImage();
  }
}
