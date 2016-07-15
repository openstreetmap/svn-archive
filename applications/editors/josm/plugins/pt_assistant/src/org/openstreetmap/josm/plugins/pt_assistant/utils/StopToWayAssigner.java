package org.openstreetmap.josm.plugins.pt_assistant.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.tools.Pair;

/**
 * Assigns stops to ways in following steps: (1) checks if the stop is in the
 * list of already assigned stops, (2) checks if the stop has a stop position,
 * (3) calculates it using proximity / growing bounding boxes
 * 
 * @author darya
 *
 */
public class StopToWayAssigner {

	/* contains assigned stops */
	private static HashMap<PTStop, PTWay> stopToWay = new HashMap<>();

	/*
	 * contains all PTWays of the route relation for which this assigner was
	 * created
	 */
	private List<PTWay> ptways;

	public StopToWayAssigner(List<PTWay> ptways) {
		this.ptways = ptways;
	}

	/**
	 * Returns the PTWay for the given PTStop
	 * 
	 * @param stop
	 * @return
	 */
	public PTWay get(PTStop stop) {

		// 1) Search if this stop has already been assigned:
		if (stopToWay.containsKey(stop)) {
			return stopToWay.get(stop);
		}

		// 2) Search if the stop has a stop position:
		PTWay ptwayOfStopPosition = findPtwayForNode(stop.getStopPosition());
		if (ptwayOfStopPosition != null) {
			stopToWay.put(stop, ptwayOfStopPosition);
			return ptwayOfStopPosition;
		}

		// 3) Search if the stop has a stop_area:
		List<OsmPrimitive> stopElements = new ArrayList<>(2);
		if (stop.getStopPosition() != null) {
			stopElements.add(stop.getStopPosition());
		}
		if (stop.getPlatform() != null) {
			stopElements.add(stop.getPlatform());
		}
		Set<Relation> parents = Node.getParentRelations(stopElements);
		for (Relation parentRelation : parents) {
			if (parentRelation.hasTag("public_transport", "stop_area")) {
				for (RelationMember rm : parentRelation.getMembers()) {
					if (rm.getMember().hasTag("public_transport", "stop_position")) {
						PTWay rmPtway = this.findPtwayForNode(rm.getNode());
						if (rmPtway != null) {
							stopToWay.put(stop, rmPtway);
							return rmPtway;
						}
					}
				}
			}
		}

		// 4) Run the growing-bounding-boxes algorithm:
		double searchRadius = 0.001;
		while (searchRadius < 0.005) {

			PTWay foundWay = this.findNearestWayInRadius(stop.getPlatform(), searchRadius);
			if (foundWay != null) {
				stopToWay.put(stop, foundWay);
				return foundWay;
			}

			foundWay = this.findNearestWayInRadius(stop.getStopPosition(), searchRadius);
			if (foundWay != null) {
				stopToWay.put(stop, foundWay);
				return foundWay;
			}

			searchRadius = searchRadius + 0.001;
		}

		return null;
	}

	/**
	 * Finds the PTWay of the given stop_position by looking at its referrers
	 * 
	 * @param stopPosition
	 * @return
	 */
	private PTWay findPtwayForNode(Node stopPosition) {

		if (stopPosition == null) {
			return null;
		}

		// search in the referrers:
		List<OsmPrimitive> referrers = stopPosition.getReferrers();
		for (OsmPrimitive referredPrimitive : referrers) {
			if (referredPrimitive.getType().equals(OsmPrimitiveType.WAY)) {
				Way referredWay = (Way) referredPrimitive;
				for (PTWay ptway : ptways) {
					if (ptway.getWays().contains(referredWay)) {
						return ptway;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Finds the PTWay in the given radius of the OsmPrimitive. The PTWay has to
	 * belong to the route relation for which this StopToWayAssigner was
	 * created. If multiple PTWays were found, the closest one is chosen.
	 * 
	 * @param platform
	 * @param searchRadius
	 * @return
	 */
	private PTWay findNearestWayInRadius(OsmPrimitive platform, double searchRadius) {

		if (platform == null) {
			return null;
		}

		LatLon platformCenter = platform.getBBox().getCenter();
		Double ax = platformCenter.getX() - searchRadius;
		Double bx = platformCenter.getX() + searchRadius;
		Double ay = platformCenter.getY() - searchRadius;
		Double by = platformCenter.getY() + searchRadius;
		BBox platformBBox = new BBox(ax, ay, bx, by);

		List<Way> potentialWays = new ArrayList<>();

		Collection<Node> allNodes = platform.getDataSet().getNodes();
		for (Node currentNode : allNodes) {
			if (platformBBox.bounds(currentNode.getBBox())) {
				List<OsmPrimitive> referrers = currentNode.getReferrers();
				for (OsmPrimitive referrer : referrers) {
					if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
						Way referrerWay = (Way) referrer;
						for (PTWay ptway : this.ptways) {
							if (ptway.getWays().contains(referrerWay)) {
								potentialWays.add(referrerWay);
							}
						}
					}
				}

			}
		}
		
		Node platformNode = null;
		if (platform.getType().equals(OsmPrimitiveType.NODE)) {
			platformNode = (Node) platform;
		} else {
			platformNode = new Node(platform.getBBox().getCenter());
		}
		Way nearestWay = null;
		Double minDistance = Double.MAX_VALUE;
		for (Way potentialWay: potentialWays) {
			double distance = this.calculateMinDistance(platformNode, potentialWay);
			if (distance < minDistance) {
				minDistance = distance;
				nearestWay = potentialWay;
			}
		}

		for (PTWay ptway: this.ptways) {
			if (ptway.getWays().contains(nearestWay)) {
				return ptway;
			}
		}
		
		return null;
	}

	/**
	 * Calculates the minimum distance between a node and a way
	 * @param node
	 * @param way
	 * @return
	 */
	private double calculateMinDistance(Node node, Way way) {

		double minDistance = Double.MAX_VALUE;

		List<Pair<Node, Node>> waySegments = way.getNodePairs(false);
		for (Pair<Node, Node> waySegment : waySegments) {
			if (waySegment.a != node && waySegment.b != node) {
				double distanceToLine = this.calculateDistance(node, waySegment);
				if (distanceToLine < minDistance) {
					minDistance = distanceToLine;
				}
			}
		}

		return minDistance;

	}

	/**
	 * // * Calculates the distance from point to segment using formulas for
	 * triangle area.
	 * 
	 * @param node
	 * @param waySegment
	 * @return
	 */
	private double calculateDistance(Node node, Pair<Node, Node> segment) {

		/*
		 * Let a be the triangle edge between the point and the first node of
		 * the segment. Let b be the triangle edge between the point and the
		 * second node of the segment. Let c be the triangle edge which is the
		 * segment.
		 */

		double lengthA = node.getCoor().distance(segment.a.getCoor());
		double lengthB = node.getCoor().distance(segment.b.getCoor());
		double lengthC = segment.a.getCoor().distance(segment.b.getCoor());

		// calculate triangle area using Feron's formula:
		double p = (lengthA + lengthB + lengthC) / 2.0;
		double triangleArea = Math.sqrt(p * (p - lengthA) * (p - lengthB) * (p - lengthC));

		// calculate the distance from point to segment using the 0.5*c*h
		// formula for triangle area:
		return triangleArea * 2.0 / lengthC;
	}

	/**
	 * May be needed if the correspondence between stops and ways has changed
	 * significantly
	 */
	public static void reinitiate() {
		stopToWay = new HashMap<>();
	}

}
