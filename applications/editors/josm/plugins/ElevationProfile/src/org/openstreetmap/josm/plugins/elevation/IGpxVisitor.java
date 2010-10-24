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

import org.openstreetmap.josm.data.gpx.GpxRoute;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * Interface for all GPX visitors.
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public interface IGpxVisitor extends IGpxWaypointVisitor {
	/**
	 * Starts a GPX route.
	 * @param route The route to visit.
	 */
	void start(GpxRoute route);
	
	/**
	 * Ends a GPX route.
	 * @param route The route to visit.
	 */
	void end(GpxRoute route);
	
	/**
	 * Visits a way point within a GPX route.
	 * @param route The route containing the way point.
	 * @param wp The way point to visit.
	 */
	void visit(GpxRoute route, WayPoint wp);
	
	/**
	 * Called before a GPX track is iterated.
	 * @param track The track to visit.
	 */
	void start(GpxTrack track);
	
	/**
	 * Called after a track iteration.
	 * @param track The track to visit.
	 */
	void end(GpxTrack track);
	
	/**
	 * Visits a way point within a GPX track.
	 * @param track The associated track of the way point.
	 * @param segment The associated segment of the way point.
	 * @param wp The way point to visit.
	 */
	void visit(GpxTrack track, GpxTrackSegment segment, WayPoint wp);
}
