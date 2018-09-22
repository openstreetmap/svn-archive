// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.RasterImageGeoreferencer;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.tools.Logging;

/**
 * Improve georeferencing (only raster images)
 */
public class MenuActionRefineGeoRef extends JosmAction {

    private static final String NAME = marktr("Refine georeferencing");

    private WMSLayer wmsLayer;
    private RasterImageGeoreferencer rasterImageGeoreferencer;

    /**
     * Constructs a new {@code MenuActionRefineGeoRef}.
     * @param wmsLayer WMS layer
     */
    public MenuActionRefineGeoRef(WMSLayer wmsLayer) {
        super(tr(NAME), null, tr("Improve georeferencing (only raster images)"), null, false);
        this.wmsLayer = wmsLayer;
        rasterImageGeoreferencer = new RasterImageGeoreferencer();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!wmsLayer.isRaster()) {
            Logging.info("MenuActionRefineGeoRef called for unexpected layer type");
            return;
        }
        if (CadastrePlugin.isCadastreProjection()) {
            //wmsLayer = WMSDownloadAction.getLayer();
        } else {
            CadastrePlugin.askToChangeProjection();
        }
        rasterImageGeoreferencer.addListener();
        rasterImageGeoreferencer.startGeoreferencing(wmsLayer);
    }
}
