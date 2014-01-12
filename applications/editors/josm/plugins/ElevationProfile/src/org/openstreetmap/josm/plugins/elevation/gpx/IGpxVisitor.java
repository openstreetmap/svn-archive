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
	 * Starts a GPX way point collection.
	 */
	void beginWayPoints();
	
	/**
	 * Ends a GPX way point collection.
	 */
	void endWayPoints();
	
	/**
	 * Starts a GPX track.
	 */
	void beginTrack(GpxTrack track);
	
	/**
	 * Ends a GPX track.
	 */
	void endTrack(GpxTrack track);
	
	/**
	 * Starts a GPX route.
	 */
	void beginRoute(GpxRoute track);
	
	/**
	 * Ends a GPX route.
	 */
	void endRoute(GpxRoute track);

	
	/**
	 * Starts a segment within a GPX track.
	 */
	void beginTrackSegment(GpxTrack track, GpxTrackSegment segment);
	
	/**
	 * Ends a segment within a GPX track.
	 */
	void endTrackSegment(GpxTrack track, GpxTrackSegment segment);
	
	/**
	 * Visits a way point within a GPX route.
	 * @param route The route containing the way point.
	 * @param wp The way point to visit.
	 */
	void visitWayPoint(WayPoint wp);
	
	/**
	 * Visits a way point within a GPX track.
	 *
	 * @param wp The way point to visit.
	 * @param track the track containing the way point.
	 * @param segment the track segment
	 */
	void visitTrackPoint(WayPoint wp, GpxTrack track, GpxTrackSegment segment);
	
	/**
	 * Visits a way point within a GPX route.
	 * @param route the route containing the way point.
	 * @param wp the way point to visit.
	 */
	void visitRoutePoint(WayPoint wp, GpxRoute route);
}
