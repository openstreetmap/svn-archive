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

package org.openstreetmap.josm.plugins.elevation.gui;

import java.awt.Color;
import java.awt.Graphics;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.elevation.gpx.ElevationWayPointKind;
import org.openstreetmap.josm.plugins.elevation.gpx.IElevationProfile;

/**
 * Basic interface for all elevation profile renderers. First, therenderer determines the color
 * for a given way point, so that as well the dialog as the layer can share the color scheme.
 * Second, the layer can simply pass the painting stuff to a renderer without taking care of
 * details. 
 * 
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */
public interface IElevationProfileRenderer {
	/**
	 * Gets the color for a given way point.
	 * @param profile The elevation profile that contains the way point.
	 * @param wpt The way point to get the color for.
	 * @param kind The way point kind (see {@link ElevationWayPointKind}).
	 * @return The color for the way point or null, if invalid arguments have been specified.
	 */
	Color getColorForWaypoint(IElevationProfile profile, WayPoint wpt, ElevationWayPointKind kind);
	
	/**
	 * Renders the way point with the lowest elevation.
	 * @param g The graphics context.
	 * @param profile The elevation profile that contains the way point.
	 * @param wpt The way point to render.
	 * @param kind The way point kind (see {@link ElevationWayPointKind}).	 
	 */
	void renderWayPoint(Graphics g, IElevationProfile profile, MapView mv, WayPoint wpt, ElevationWayPointKind kind);
	
	/**
	 * Notifies the renderer that rendering starts.
	 */
	void beginRendering();
	
	/**
	 * Notifies the renderer that rendering has been finished.
	 */
	void finishRendering();
}
