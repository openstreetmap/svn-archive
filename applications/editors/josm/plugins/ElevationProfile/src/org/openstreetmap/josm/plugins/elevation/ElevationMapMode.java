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

package org.openstreetmap.josm.plugins.elevation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Provides the map mode and controls visibility of the elevation profile layer/panel.
 */
public class ElevationMapMode extends MapMode implements IElevationModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1011179566962655639L;
	

	public ElevationMapMode(String name, MapFrame mapFrame) {		
		super(name, 
				"elevation.png", 
				tr("Shows elevation profile"), 
				mapFrame, 
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));		
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.IElevationModelListener#elevationProfileChanged(org.openstreetmap.josm.plugins.elevation.IElevationProfile)
	 */
	public void elevationProfileChanged(IElevationProfile profile) {
		ElevationProfilePlugin.getCurrentLayer().setProfile(profile);
	}

	@Override
	public void enterMode() {
		super.enterMode();
		ElevationProfilePlugin.getCurrentLayer().setVisible(true);
	}

	@Override
	public void exitMode() {
		super.exitMode();
		ElevationProfilePlugin.getCurrentLayer().setVisible(false);
	}
}
