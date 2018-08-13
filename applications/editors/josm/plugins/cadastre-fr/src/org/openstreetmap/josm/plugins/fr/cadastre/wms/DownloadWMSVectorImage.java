// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.Logging;

public class DownloadWMSVectorImage extends PleaseWaitRunnable {

    private WMSLayer wmsLayer;
    private Bounds bounds;
    private static String errorMessage;

    public DownloadWMSVectorImage(WMSLayer wmsLayer, Bounds bounds) {
        super(tr("Downloading {0}", wmsLayer.getName()));

        this.wmsLayer = wmsLayer;
        this.bounds = bounds;
    }

    @Override
    public void realRun() throws IOException {
        progressMonitor.indeterminateSubTask(tr("Contacting WMS Server..."));
        errorMessage = null;
        try {
            if (wmsLayer.grabber.getWmsInterface().retrieveInterface(wmsLayer)) {
                if (!wmsLayer.hasImages()) {
                    // first time we grab an image for this layer
                    if (CacheControl.cacheEnabled) {
                        if (wmsLayer.grabThread.getCacheControl().loadCacheIfExist()) {
                            MainApplication.getMap().mapView.zoomTo(wmsLayer.getFirstViewFromCacheBBox().toBounds());
                            return;
                        }
                    }
                    if (wmsLayer.isRaster()) {
                        // set raster image commune bounding box based on current view (before adjustment)
                        GuiHelper.runInEDT(() ->
                            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                tr("This commune is not vectorized.\nPlease use the other menu entry to georeference a \"Plan image\"")));
                        MainApplication.getLayerManager().removeLayer(wmsLayer);
                        wmsLayer = null;
                        return;
                    } else {
                        // set vectorized commune bounding box by opening the standard web window
                        wmsLayer.grabber.getWmsInterface().retrieveCommuneBBox(wmsLayer);
                    }
                }
                // grab new images from wms server into active layer
                wmsLayer.grab(bounds);
            } else if (!wmsLayer.hasImages()) {
              // failed to contact WMS of find this commune. Remove layer if empty.
              MainApplication.getLayerManager().removeLayer(wmsLayer);
            }
        } catch (DuplicateLayerException e) {
            // we tried to grab onto a duplicated layer (removed)
            Logging.warn("removed a duplicated layer");
        } catch (WMSException e) {
            Logging.warn(e);
            errorMessage = e.getMessage();
            wmsLayer.grabber.getWmsInterface().resetCookie();
        }
    }

    @Override
    protected void cancel() {
        wmsLayer.grabber.getWmsInterface().cancel();
        if (wmsLayer != null)
            wmsLayer.grabThread.setCanceled(true);
    }

    @Override
    protected void finish() {
    }

    public static void download(WMSLayer wmsLayer) {
        MapView mv = MainApplication.getMap().mapView;
        Bounds bounds = new Bounds(mv.getLatLon(0, mv.getHeight()), mv.getLatLon(mv.getWidth(), 0));

        MainApplication.worker.execute(new DownloadWMSVectorImage(wmsLayer, bounds));
        if (errorMessage != null)
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), errorMessage);
    }
}
