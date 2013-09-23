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

import org.openstreetmap.josm.plugins.elevation.gpx.ElevationModel;
import org.openstreetmap.josm.plugins.elevation.gpx.IElevationProfile;

/**
 * This interface is intended to allow clients reaction on changes in the elevation model changes (e. g. 
 * repaint UI widgets).
 * {@link ElevationModel}
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public interface IElevationModelListener {
	/**
	 * Notifies listeners that the selected track has been changed.
	 * @param model The model changed.
	 */
	void elevationProfileChanged(IElevationProfile model);
}
