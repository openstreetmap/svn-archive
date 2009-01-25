package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.data.Bounds;

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
        Main.pleaseWaitDlg.currentAction.setText(tr("Contacting WMS Server..."));
        try {
            if (grabber.getWmsInterface().retrieveInterface(wmsLayer)) {
                if (wmsLayer.isRaster() && wmsLayer.images.isEmpty())
                    wmsLayer.setRasterBounds(bounds);
                if (CacheControl.cacheEnabled && wmsLayer.images.isEmpty()) {
                    // images loaded from cache
                    if (wmsLayer.getCacheControl().loadCacheIfExist()) {
                        Main.map.mapView.repaint();
                        return;
                    }
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
        //wmsLayer.saveToCache();
    }

    public static void download(WMSLayer wmsLayer) {
        MapView mv = Main.map.mapView;
        Bounds bounds = new Bounds(mv.getLatLon(0, mv.getHeight()), mv.getLatLon(mv.getWidth(), 0));

        Main.worker.execute(new DownloadWMSTask(wmsLayer, bounds));

    }
}
