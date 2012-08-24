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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.elevation.gui.ElevationProfileDialog;
import org.openstreetmap.josm.plugins.elevation.gui.ElevationProfileLayer;

/**
 * Plugin class for displaying an elevation profile of the tracks.
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */
public class ElevationProfilePlugin extends Plugin {
	
	private ElevationMapMode eleMode;
	private IconToggleButton eleModeButton;
	private static ElevationProfileLayer currentLayer;

	/**
	 * Initializes the plugin.
	 * @param info Context information about the plugin.
	 */
	public ElevationProfilePlugin(PluginInformation info) {
		super(info);

		try {
			eleMode = new ElevationMapMode("Elevation profile", Main.map);
			eleModeButton = new IconToggleButton(eleMode);
		} catch (Exception e1) {
			System.err.println("Init of ElevationProfilePlugin failed: " + e1);
			e1.printStackTrace();
		}
	}
	
	/**
     * Called after Main.mapFrame is initialized. (After the first data is loaded).
     * You can use this callback to tweak the newFrame to your needs, as example install
     * an alternative Painter.
     */
	@Override	
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		super.mapFrameInitialized(oldFrame, newFrame);
		
		if (newFrame != null) {
			newFrame.toolGroup.add(eleModeButton);
			ElevationProfileDialog eleProfileDlg = new ElevationProfileDialog();
			eleProfileDlg.addModelListener(eleMode);
			eleProfileDlg.setProfileLayer(getCurrentLayer());
			newFrame.addToggleDialog(eleProfileDlg);
		}
	}
	
	/**
	 * Gets the elevation profile layer which decorates the current layer
	 * with some markers.
	 * @return
	 */
	public static ElevationProfileLayer getCurrentLayer(){
		if(currentLayer == null){
			currentLayer = new ElevationProfileLayer("Elevation profile");
			Main.main.addLayer(currentLayer);			
		}
		return currentLayer;
	}

	
}
