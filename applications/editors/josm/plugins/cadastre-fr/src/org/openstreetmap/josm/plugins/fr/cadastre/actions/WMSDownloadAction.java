// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

//import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MainApplication;
//import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.spi.preferences.Config;

final class WMSDownloadAction {

    private WMSDownloadAction() {
        // Hide default constructor
    }

    static WMSLayer getLayer() {
        // check if we already have a layer created. if not, create; if yes, reuse.
        ArrayList<WMSLayer> existingWMSlayers = new ArrayList<>();
        if (MainApplication.getMap() != null) {
            Layer activeLayer = MainApplication.getLayerManager().getActiveLayer();
            if (activeLayer instanceof WMSLayer)
                return (WMSLayer) activeLayer;
            for (Layer l : MainApplication.getLayerManager().getLayers()) {
                if (l instanceof WMSLayer) {
                    existingWMSlayers.add((WMSLayer) l);
                }
            }
            if (existingWMSlayers.size() == 1)
                return existingWMSlayers.get(0);
            if (existingWMSlayers.size() == 0)
                return new MenuActionNewLocation().addNewLayer(existingWMSlayers);
            if (Config.getPref().getBoolean("cadastrewms.autoFirstLayer", false)) {
                return existingWMSlayers.get(0);
            } else {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        tr("More than one WMS layer present\nSelect one of them first, then retry"));
            }
        } else {
            return new MenuActionNewLocation().addNewLayer(existingWMSlayers);
        }
        return null;
    }
}
