// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;

/**
 * Cancel current grab (only vector images)
 */
public class MenuActionCancelGrab extends JosmAction {

    private static final String NAME = marktr("Cancel current grab");

    private WMSLayer wmsLayer;

    /**
     * Constructs a new {@code MenuActionCancelGrab}.
     * @param wmsLayer WMS layer
     */
    public MenuActionCancelGrab(WMSLayer wmsLayer) {
        super(tr(NAME), null, tr("Cancel current grab (only vector images)"), null, false);
        this.wmsLayer = wmsLayer;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (wmsLayer.grabThread.getImagesToGrabSize() > 0) {
            wmsLayer.grabThread.cancel();
        }
    }
}
