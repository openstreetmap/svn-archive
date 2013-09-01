package org.openstreetmap.josm.plugins.elevation.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.elevation.ColoredElevationLayer;

public class AddElevationLayerAction extends JosmAction {

    /**
     * 
     */
    private static final long serialVersionUID = -745642875640041385L;
    private ColoredElevationLayer currentLayer;

    public AddElevationLayerAction() {
	super(tr("Elevation Map Layer"), "elevation", tr("Shows elevation layer"), null, true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
	if (currentLayer == null) {
	    currentLayer = new ColoredElevationLayer(tr("Elevation Map")); // TODO: Better name
	    Main.main.addLayer(currentLayer);
	}

    }

}
