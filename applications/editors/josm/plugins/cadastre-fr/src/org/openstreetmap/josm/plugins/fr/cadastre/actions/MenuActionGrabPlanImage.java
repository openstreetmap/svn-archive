// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.DownloadWMSPlanImage;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.RasterImageGeoreferencer;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Grab non-georeferenced image
 */
public class MenuActionGrabPlanImage extends JosmAction implements Runnable {

    /**
     * Action calling the wms grabber for non georeferenced images called "plan image"
     */
    private static final long serialVersionUID = 1L;

    public static final String NAME = marktr("Georeference an image");

    private DownloadWMSPlanImage downloadWMSPlanImage;
    private WMSLayer wmsLayer;
    private RasterImageGeoreferencer rasterImageGeoreferencer;

    /**
     * Constructs a new {@code MenuActionGrabPlanImage}.
     */
    public MenuActionGrabPlanImage() {
        super(tr(NAME), "cadastre_small", tr("Grab non-georeferenced image"), null, false, "cadastrefr/grabplanimage", true);
        rasterImageGeoreferencer = new RasterImageGeoreferencer();
    }

    @Override
    protected void updateEnabledState() {
        if (wmsLayer == null || !MainApplication.isDisplayingMapView()) return;
        if (!rasterImageGeoreferencer.isRunning()) return;
        if (MainApplication.getLayerManager().containsLayer(wmsLayer))
            return;
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Georeferencing interrupted"));
        rasterImageGeoreferencer.actionInterrupted();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (MainApplication.getMap() != null) {
            if (CadastrePlugin.isCadastreProjection()) {
                wmsLayer = new MenuActionNewLocation().addNewLayer(new ArrayList<WMSLayer>());
                if (wmsLayer == null) return;
                downloadWMSPlanImage = new DownloadWMSPlanImage();
                downloadWMSPlanImage.download(wmsLayer);
                // download sub-images of the cadastre scan and join them into one single
                MainApplication.worker.execute(this);
            } else {
                CadastrePlugin.askToChangeProjection();
            }
        }
    }

    @Override
    public void run() {
        // wait until plan image is fully loaded and joined into one single image
        boolean loadedFromCache = downloadWMSPlanImage.waitFinished();
        if (loadedFromCache) {
            wmsLayer.invalidate();
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
                if (Config.getPref().getBoolean("cadastrewms.noImageCropping", false) == false)
                    rasterImageGeoreferencer.startCropping(wmsLayer);
                else
                    rasterImageGeoreferencer.startGeoreferencing(wmsLayer);
            }
        }
    }
}
