// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.Layer;

public class MenuActionGrabPlanImage extends JosmAction implements Runnable {

    /**
     * Action calling the wms grabber for non georeferenced images called "plan image"
     */
    private static final long serialVersionUID = 1L;

    public static String name = marktr("Georeference an image");

    private DownloadWMSPlanImage downloadWMSPlanImage;
    private WMSLayer wmsLayer;
    private RasterImageGeoreferencer rasterImageGeoreferencer;
    
    public MenuActionGrabPlanImage() {
        super(tr(name), "cadastre_small", tr("Grab non-georeferenced image"), null, false, "cadastrefr/grabplanimage", true);
        rasterImageGeoreferencer = new RasterImageGeoreferencer();
    }

    @Override
    protected void updateEnabledState() {
        if (wmsLayer == null || Main.map == null || Main.map.mapView == null) return;
        if (!rasterImageGeoreferencer.isRunning()) return;
        for (Layer l : Main.map.mapView.getAllLayersAsList())
            if (l == wmsLayer)
                return;
        JOptionPane.showMessageDialog(Main.parent, tr("Georeferencing interrupted"));
        rasterImageGeoreferencer.actionInterrupted();
    }

    public void actionPerformed(ActionEvent ae) {
        if (Main.map != null) {
            if (CadastrePlugin.isCadastreProjection()) {
                wmsLayer = new MenuActionNewLocation().addNewLayer(new ArrayList<WMSLayer>());
                if (wmsLayer == null) return;
                downloadWMSPlanImage = new DownloadWMSPlanImage();
                downloadWMSPlanImage.download(wmsLayer);
                // download sub-images of the cadastre scan and join them into one single
                Main.worker.execute(this);
            } else {
                CadastrePlugin.askToChangeProjection();
            }
        }
    }

    public void run() {
        // wait until plan image is fully loaded and joined into one single image
        boolean loadedFromCache = downloadWMSPlanImage.waitFinished();
        if (loadedFromCache) {
            Main.map.repaint();
        } else if (wmsLayer.getImages().size() == 0) {
            // action canceled or image loaded from cache (and already georeferenced)
            rasterImageGeoreferencer.actionInterrupted();
        } else {
            int reply = JOptionPane.CANCEL_OPTION;
            if (wmsLayer.isAlreadyGeoreferenced()) {
                reply = JOptionPane.showConfirmDialog(null,
                        tr("This image contains georeference data.\n"+
                                "Do you want to use them ?"),
                        null,
                        JOptionPane.YES_NO_OPTION);
            }
            if (reply == JOptionPane.OK_OPTION) {
                rasterImageGeoreferencer.transformGeoreferencedImg();
            } else {
                rasterImageGeoreferencer.addListener();
                if (Main.pref.getBoolean("cadastrewms.noImageCropping", false) == false)
                    rasterImageGeoreferencer.startCropping(wmsLayer);
                else
                    rasterImageGeoreferencer.startGeoreferencing(wmsLayer);
            }
        }
    }
}
