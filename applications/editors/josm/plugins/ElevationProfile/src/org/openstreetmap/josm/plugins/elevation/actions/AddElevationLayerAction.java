/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */
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
