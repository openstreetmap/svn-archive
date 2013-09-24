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

import java.util.Collection;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxRoute;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * Utility class to apply a visitor on GPX containers (track, route, data).
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public class GpxIterator {
	/**
	 * Static class, no need to instantiate me.
	 */
	private GpxIterator() {}
	
	/**
	 * Runs the given visitor on a GPX data instance. If one or both 
	 * arguments are null, this method will return immediately. 
	 * 
	 * @param data
	 *            The GPX data instance.
	 * @param visitor
	 *            The visitor which inspects all GPX entities.
	 */
	public static void visit(GpxData data, IGpxVisitor visitor) {
		if (data == null) return;
		if (visitor == null) return;
		
		if (data.isEmpty()) return;
		
		visitor.start();
		visitSingleWaypoints(data, visitor);
		visitor.end();

		// routes
		if (data.hasRoutePoints()) {
			for (GpxRoute rte : data.routes) {
				visitRoute(visitor, rte);
			}
		}

		// tracks
		for (GpxTrack trk : data.tracks) {
			visitTrack(visitor, trk);
		}
	}
	
	/**
	 * Visits a single GPX track. 
	 * @param track The track to visit.
	 * @param visitor
	 *            The visitor which inspects all GPX entities.
	 */
	public static void visit(GpxTrack track, IGpxVisitor visitor) {
		visitTrack(visitor, track);
	}
	
	/**
	 * Visits a single GPX route. 
	 * @param route The route to visit.
	 * @param visitor
	 *            The visitor which inspects all GPX entities.
	 */
	public static void visit(GpxRoute route, IGpxVisitor visitor) {
		visitRoute(visitor, route);
	}

	// ---------------------- Helper methods ----------------
	
	/**
	 * @param visitor
	 * @param trk
	 */
	private static void visitTrack(IGpxVisitor visitor, GpxTrack trk) {
		if (trk == null) return;
		if (visitor == null) return;
		
		Collection<GpxTrackSegment> segments = trk.getSegments();
		
		if (segments != null) {
		    	visitor.start(trk);
		    	// visit all segments
			for (GpxTrackSegment segment : segments) {
			    Collection<WayPoint> waypts = segment.getWayPoints();
			    	// no visitor here...
				if (waypts == null)
					continue;
				
			        visitor.start(trk, segment);
			        
				for (WayPoint wayPoint : waypts) {
					visitor.visit(wayPoint);
				}
				
				visitor.end(trk, segment);
			}
			visitor.end(trk);
		}		
		
	}

	/**
	 * @param visitor
	 * @param route
	 */
	private static void visitRoute(IGpxVisitor visitor, GpxRoute route) {
		if (route == null) return;
		if (visitor == null) return;
		
		visitor.start();
		for (WayPoint wpt : route.routePoints) {
			visitor.visit(wpt);
		}
		visitor.end();
	}

	/**
	 * @param data
	 * @param visitor
	 */
	private static void visitSingleWaypoints(GpxData data, IGpxVisitor visitor) {
		// isolated way points
		if (data.waypoints != null) { // better with an hasWaypoints method!?
			for (WayPoint wpt : data.waypoints) {
				visitor.visit(wpt);
			}
		}
	}
}
