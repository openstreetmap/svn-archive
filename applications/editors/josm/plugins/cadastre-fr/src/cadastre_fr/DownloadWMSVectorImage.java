package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;

public class DownloadWMSTask extends PleaseWaitRunnable {

    private WMSLayer wmsLayer;

    private Bounds bounds;

    private CadastreGrabber grabber = CadastrePlugin.cadastreGrabber;

    public DownloadWMSTask(WMSLayer wmsLayer, Bounds bounds) {
        super(tr("Downloading {0}", wmsLayer.name));

        this.wmsLayer = wmsLayer;
        this.bounds = bounds;
    }

    @Override
    public void realRun() throws IOException {
        progressMonitor.indeterminateSubTask(tr("Contacting WMS Server..."));
        try {
            if (grabber.getWmsInterface().retrieveInterface(wmsLayer)) {
                if (wmsLayer.images.isEmpty()) {
                    // first time we grab an image for this layer
                    if (CacheControl.cacheEnabled) {
                        if (wmsLayer.getCacheControl().loadCacheIfExist()) {
                            Main.map.mapView.repaint();
                            return;
                        }
                    }
                    if (wmsLayer.isRaster())
                        // set raster image commune bounding box based on current view (before adjustment)
                        wmsLayer.setRasterBounds(bounds);
                    else
                        // set vectorized commune bounding box by opening the standard web window
                        wmsLayer.setCommuneBBox( grabber.getWmsInterface().retrieveCommuneBBox());
                }
                // grab new images from wms server into active layer
                wmsLayer.grab(grabber, bounds);
            }
        } catch (DuplicateLayerException e) {
            // we tried to grab onto a duplicated layer (removed)
            System.err.println("removed a duplicated layer");
        }
    }

    @Override
    protected void cancel() {
        grabber.getWmsInterface().cancel();
    }

    @Override
    protected void finish() {
    }

    public static void download(WMSLayer wmsLayer) {
        MapView mv = Main.map.mapView;
        Bounds bounds = new Bounds(mv.getLatLon(0, mv.getHeight()), mv.getLatLon(mv.getWidth(), 0));

        Main.worker.execute(new DownloadWMSTask(wmsLayer, bounds));

    }
}
