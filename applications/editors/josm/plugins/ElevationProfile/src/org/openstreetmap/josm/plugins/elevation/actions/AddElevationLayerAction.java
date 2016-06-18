// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.elevation.grid.ElevationGridLayer;

public class AddElevationLayerAction extends JosmAction {

    /**
     * 
     */
    private static final long serialVersionUID = -745642875640041385L;
    private Layer currentLayer;

    public AddElevationLayerAction() {
        super(tr("Elevation Grid Layer (experimental!)"), "elevation", tr("Shows elevation grid layer"), null, true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (currentLayer == null) {
            currentLayer = new ElevationGridLayer(tr("Elevation Grid")); // TODO: Better name
            Main.main.addLayer(currentLayer);
        }
    }
}
