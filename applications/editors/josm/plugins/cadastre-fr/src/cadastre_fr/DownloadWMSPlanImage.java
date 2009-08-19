package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;

public class DownloadWMSPlanImage {
    
    private Future<Task> task = null;
    private WMSLayer wmsLayer;
    private Bounds bounds;
    private boolean dontGeoreference = false;
    
    private class Task extends PleaseWaitRunnable {
        private CadastreGrabber grabber = CadastrePlugin.cadastreGrabber;
        public Task(WMSLayer wmsLayer, Bounds bounds) {
            super(tr("Downloading {0}", wmsLayer.getName()));
        }

        @Override
        public void realRun() throws IOException {
            progressMonitor.indeterminateSubTask(tr("Contacting cadastre WMS ..."));
            try {
                if (grabber.getWmsInterface().retrieveInterface(wmsLayer)) {
                    if (!wmsLayer.images.isEmpty()) {
                        //JOptionPane.showMessageDialog(Main.parent,tr("Image already loaded"));
                        JOptionPane pane = new JOptionPane(
                                tr("Image already loaded")
                                , JOptionPane.INFORMATION_MESSAGE);
                        // this below is a temporary workaround to fix the "always on top" issue
                        JDialog dialog = pane.createDialog(Main.parent, "");
                        CadastrePlugin.prepareDialog(dialog);
                        dialog.setVisible(true);
                        // till here
                        dontGeoreference = true;
                    } else if (grabber.getWmsInterface().downloadCancelled){
                        // do nothing
                    } else {
                        // first time we grab an image for this layer
                        if (CacheControl.cacheEnabled) {
                            if (wmsLayer.getCacheControl().loadCacheIfExist()) {
                                dontGeoreference = true;
                                Main.map.mapView.repaint();
                                return;
                            }
                        }
                        if (wmsLayer.isRaster()) {
                            // set raster image commune bounding box based on current view (before adjustment)
                            wmsLayer.setCommuneBBox( grabber.getWmsInterface().retrieveCommuneBBox());
                            wmsLayer.setRasterBounds(bounds);
                            // grab new images from wms server into active layer
                            wmsLayer.grab(grabber, bounds);
                            if (grabber.getWmsInterface().downloadCancelled) {
                                wmsLayer.images.clear();
                                Main.map.mapView.repaint();
                            } else {
                                // next steps follow in method finish() when download is terminated
                                wmsLayer.joinRasterImages();
                            }
                        } else {
                            /*JOptionPane.showMessageDialog(Main.parent,tr("Municipality vectorized !\n"+
                                    "Use the normal Cadastre Grab menu."));*/
                            JOptionPane pane = new JOptionPane(
                                    tr("Municipality vectorized !\nUse the normal Cadastre Grab menu.")
                                    , JOptionPane.INFORMATION_MESSAGE);
                            // this below is a temporary workaround to fix the "always on top" issue
                            JDialog dialog = pane.createDialog(Main.parent, "");
                            CadastrePlugin.prepareDialog(dialog);
                            dialog.setVisible(true);
                            // till here
                        }
                    }
                }
            } catch (DuplicateLayerException e) {
                // we tried to grab onto a duplicated layer (removed)
                System.err.println("removed a duplicated layer");
            }
        }
        
        @Override
        protected void cancel() {
            grabber.getWmsInterface().cancel();
            dontGeoreference = true;
        }

        @Override
        protected void finish() {
        }
    }
    
    public void download(WMSLayer wmsLayer) {
        MapView mv = Main.map.mapView;
        Bounds bounds = new Bounds(mv.getLatLon(0, mv.getHeight()), mv.getLatLon(mv.getWidth(), 0));

        //Main.worker.execute(new DownloadWMSPlanImage(wmsLayer, bounds));
        Task t = new Task(wmsLayer, bounds);
        this.wmsLayer = wmsLayer;
        this.bounds = bounds;
        task = Main.worker.submit(t, t);
    }

    public boolean waitFinished() {
        if (task != null) {
            try {
                task.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dontGeoreference;
    }
}
