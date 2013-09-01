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
 * Interface for all GPX visitors.
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public interface IGpxVisitor extends IGpxWaypointVisitor {
	/**
	 * Starts a GPX route, track or way point collection.
	 */
	void start();
	
	/**
	 * Ends a GPX route, track or way point collection.
	 */
	void end();
	
	/**
	 * Visits a way point within a GPX route.
	 * @param route The route containing the way point.
	 * @param wp The way point to visit.
	 */
	void visit(WayPoint wp);
}
