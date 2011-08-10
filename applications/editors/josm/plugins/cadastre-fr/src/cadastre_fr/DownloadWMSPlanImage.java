// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
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
    private static boolean dontGeoreference = false;
    private static String errorMessage;
    
    private class Task extends PleaseWaitRunnable {
        public Task(WMSLayer wmsLayer, Bounds bounds) {
            super(tr("Downloading {0}", wmsLayer.getName()));
        }

        @Override
        public void realRun() throws IOException {
            progressMonitor.indeterminateSubTask(tr("Contacting cadastre WMS ..."));
            errorMessage = null;
            try {
                if (wmsLayer.grabber.getWmsInterface().retrieveInterface(wmsLayer)) {
                    if (!wmsLayer.getImages().isEmpty()) {
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
                    } else if (wmsLayer.grabber.getWmsInterface().downloadCanceled){
                        // do nothing
                    } else {
                        // first time we grab an image for this layer
                        if (CacheControl.cacheEnabled) {
                            if (wmsLayer.grabThread.getCacheControl().loadCacheIfExist()) {
                                dontGeoreference = true;
                                Main.map.mapView.repaint();
                                return;
                            }
                        }
                        if (wmsLayer.isRaster()) {
                            // set raster image commune bounding box based on current view (before adjustment)
                            wmsLayer.grabber.getWmsInterface().retrieveCommuneBBox(wmsLayer);
                            wmsLayer.setRasterBounds(bounds);
                            // grab new images from wms server into active layer
                            wmsLayer.grab(bounds);
                            if (wmsLayer.grabber.getWmsInterface().downloadCanceled) {
                                wmsLayer.clearImages();
                                Main.map.mapView.repaint();
                            } else {
                                // next steps follow in method finish() when download is terminated
                                wmsLayer.joinBufferedImages();
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
            } catch (WMSException e) {
                errorMessage = e.getMessage();
                wmsLayer.grabber.getWmsInterface().resetCookie();
            }
        }
        
        @Override
        protected void cancel() {
            wmsLayer.grabber.getWmsInterface().cancel();
            dontGeoreference = true;
        }

        @Override
        protected void finish() {
        }
    }
    
    public void download(WMSLayer wmsLayer) {
        MapView mv = Main.map.mapView;
        Bounds bounds = new Bounds(mv.getLatLon(0, mv.getHeight()), mv.getLatLon(mv.getWidth(), 0));
        dontGeoreference = false;

        //Main.worker.execute(new DownloadWMSPlanImage(wmsLayer, bounds));
        Task t = new Task(wmsLayer, bounds);
        this.wmsLayer = wmsLayer;
        this.bounds = bounds;
        task = Main.worker.submit(t, t);
        if (errorMessage != null)
            JOptionPane.showMessageDialog(Main.parent, errorMessage);
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
