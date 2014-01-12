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

package org.openstreetmap.josm.plugins.elevation.gpx;

import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Interface for all GPX data visitors. Hopefully this will be part of JOSM some day.
 */
public interface IGpxWaypointVisitor {
	/**
	 * Visits a way point. This method is called for isolated way points, i. e. way points
	 * without an associated route or track. 
	 * @param wp The way point to visit.
	 */
	void visitWayPoint(WayPoint wp);
}
