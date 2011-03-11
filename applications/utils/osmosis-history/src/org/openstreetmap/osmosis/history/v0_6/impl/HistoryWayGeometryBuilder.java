// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.history.store.HistoryNodeStore;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;


/**
 * Caches a set of node latitudes and longitudes and uses these to calculate the
 * geometries for ways.
 * 
 * @author Peter Koerner
 */
public class HistoryWayGeometryBuilder {
	/**
	 * Stores the locations of nodes so that they can be used to build the way
	 * geometries.
	 */
	protected HistoryNodeStore nodeStore;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeStore
	 *            NodeStore to take the node positions from 
	 */
	public HistoryWayGeometryBuilder(HistoryNodeStore nodeStore) {
		this.nodeStore = nodeStore;
	}
	
	private Polygon createWayBbox(double left, double right, double bottom, double top) {
		Point[] points;
		LinearRing ring;
		Polygon bbox;
		
		points = new Point[5];
		points[0] = new Point(left, bottom);
		points[1] = new Point(left, top);
		points[2] = new Point(right, top);
		points[3] = new Point(right, bottom);
		points[4] = new Point(left, bottom);
		
		ring = new LinearRing(points);
		
		bbox = new Polygon(new LinearRing[] {ring});
		bbox.srid = 4326;
		
		return bbox;
	}
	
	
	/**
	 * Creates a linestring from a list of points.
	 * 
	 * @param points
	 *            The points making up the line.
	 * @return The linestring.
	 */
	public LineString createLinestring(List<Point> points) {
		LineString lineString;
		
		lineString = new LineString(points.toArray(new Point[]{}));
		lineString.srid = 4326;
		
		return lineString;
	}


    /**
     * @param nodeId
     *             Id of the node.
     * @return Point object
     */
    public Point createPoint(long nodeId, int version) {
	    Node node = nodeStore.getNode(nodeId, version);
        Point point = new Point(node.getLongitude(), node.getLatitude());
        point.srid = 4326;

        return point;
    }

	
	/**
	 * Builds a bounding box geometry object from the node references in the
	 * specified way. Unknown nodes will be ignored.
	 * 
	 * @param way
	 *            The way to create the bounding box for.
	 * @return The bounding box surrounding the way.
	 */
	public Polygon createWayBbox(Way way) {
		double left;
		double right;
		double top;
		double bottom;
		boolean nodesFound;
		
		nodesFound = false;
		left = 0;
		right = 0;
		bottom = 0;
		top = 0;
		for (WayNode wayNode : way.getWayNodes()) {
			double longitude;
			double latitude;
			
			Node node = nodeStore.findNode(wayNode.getNodeId(), way.getTimestamp());
			
			if (node != null) {
				longitude = node.getLongitude();
				latitude = node.getLatitude();
				
				if (nodesFound) {
					if (longitude < left) {
						left = longitude;
					}
					if (longitude > right) {
						right = longitude;
					}
					if (latitude < bottom) {
						bottom = latitude;
					}
					if (latitude > top) {
						top = latitude;
					}
				} else {
					left = longitude;
					right = longitude;
					bottom = latitude;
					top = latitude;
					
					nodesFound = true;
				}
			}
		}
		
		return createWayBbox(left, right, bottom, top);
	}
	
	
	/**
	 * Builds a linestring geometry object from the node references in the
	 * specified way. Unknown nodes will be ignored.
	 * 
	 * @param way
	 *            The way to create the linestring for.
	 * @return The linestring representing the way.
	 */
	public LineString createWayLinestring(Way way) {
		List<Point> linePoints;
		int numValidNodes = 0;
		
		linePoints = new ArrayList<Point>();
		for (WayNode wayNode : way.getWayNodes()) {
			
			Node node = nodeStore.findNode(wayNode.getNodeId(), way.getTimestamp());
	
			if (node  != null) {
				numValidNodes++;
				linePoints.add(new Point(node.getLongitude(), node.getLatitude()));
			} else {
				return null;
			}
		}
	
		if (numValidNodes >= 2) {	
			return createLinestring(linePoints);
		} else {
			return null;
		}
	}
}
