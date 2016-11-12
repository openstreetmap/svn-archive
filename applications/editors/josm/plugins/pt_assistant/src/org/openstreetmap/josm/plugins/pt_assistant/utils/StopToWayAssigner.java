// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
    public static HashMap<PTStop, List<Way>> stopToWay = new HashMap<>();

    /*
     * contains all PTWays of the route relation for which this assigner was
     * created
     */
    private HashSet<Way> ways;

    /* route relation for which this StopToWayAssigner was created */

    public StopToWayAssigner(List<PTWay> ptways) {
        ways = new HashSet<>();
        for (PTWay ptway : ptways) {
            ways.addAll(ptway.getWays());
        }
    }

    /**
     * Returns the PTWay for the given PTStop
     *
     * @param stop stop
     * @return the PTWay for the given PTStop
     */
    public Way get(PTStop stop) {

        // 1) Search if this stop has already been assigned:
        if (stopToWay.containsKey(stop)) {
            List<Way> assignedWays = stopToWay.get(stop);
            for (Way assignedWay : assignedWays) {
                if (this.ways.contains(assignedWay)) {
                    return assignedWay;
                }
            }
        }

        // 2) Search if the stop has a stop position:
        Way wayOfStopPosition = findWayForNode(stop.getStopPosition());
        if (wayOfStopPosition != null) {
            addAssignedWayToMap(stop, wayOfStopPosition);
            return wayOfStopPosition;
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
                        Way rmWay = this.findWayForNode(rm.getNode());
                        if (rmWay != null) {
                            addAssignedWayToMap(stop, rmWay);
                            return rmWay;
                        }
                    }
                }
            }
        }

        // 4) Search if a stop position is in the vicinity of a platform:
        if (stop.getPlatform() != null) {
            List<Node> potentialStopPositionList = stop.findPotentialStopPositions();
            Node closestStopPosition = null;
            double minDistanceSq = Double.MAX_VALUE;
            for (Node potentialStopPosition : potentialStopPositionList) {
                double distanceSq = potentialStopPosition.getCoor()
                        .distanceSq(stop.getPlatform().getBBox().getCenter());
                if (distanceSq < minDistanceSq) {
                    closestStopPosition = potentialStopPosition;
                    minDistanceSq = distanceSq;
                }
            }
            if (closestStopPosition != null) {
                Way closestWay = null;
                double minDistanceSqToWay = Double.MAX_VALUE;
                for (Way way: this.ways) {
                    if (way.containsNode(closestStopPosition)) {
                        double distanceSq = calculateMinDistanceToSegment(new Node(stop.getPlatform().getBBox().getCenter()), way);
                        if (distanceSq < minDistanceSqToWay) {
                            closestWay = way;
                            minDistanceSqToWay = distanceSq;
                        }
                    }
                }
                if (closestWay != null) {
                    addAssignedWayToMap(stop, closestWay);
                    return closestWay;
                }
            }
        }

        // 5) Run the growing-bounding-boxes algorithm:
        double searchRadius = 0.001;
        while (searchRadius < 0.005) {

            Way foundWay = this.findNearestWayInRadius(stop.getPlatform(), searchRadius);
            if (foundWay != null) {
                addAssignedWayToMap(stop, foundWay);
                return foundWay;
            }

            foundWay = this.findNearestWayInRadius(stop.getStopPosition(), searchRadius);
            if (foundWay != null) {
                addAssignedWayToMap(stop, foundWay);
                return foundWay;
            }

            searchRadius = searchRadius + 0.001;
        }

        return null;
    }

    /**
     * Finds the PTWay of the given stop_position by looking at its referrers
     *
     * @param stopPosition stop position
     * @return the PTWay of the given stop_position by looking at its referrers
     */
    private Way findWayForNode(Node stopPosition) {

        if (stopPosition == null) {
            return null;
        }

        // search in the referrers:
        List<OsmPrimitive> referrers = stopPosition.getReferrers();
        for (OsmPrimitive referredPrimitive : referrers) {
            if (referredPrimitive.getType().equals(OsmPrimitiveType.WAY)) {
                Way referredWay = (Way) referredPrimitive;
                if (this.ways.contains(referredWay)) {
                    return referredWay;
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
     * @param platform platform
     * @param searchRadius search radius
     * @return the PTWay in the given radius of the OsmPrimitive
     */
    private Way findNearestWayInRadius(OsmPrimitive platform, double searchRadius) {

        if (platform == null) {
            return null;
        }

        LatLon platformCenter = platform.getBBox().getCenter();
        Double ax = platformCenter.getX() - searchRadius;
        Double bx = platformCenter.getX() + searchRadius;
        Double ay = platformCenter.getY() - searchRadius;
        Double by = platformCenter.getY() + searchRadius;
        BBox platformBBox = new BBox(ax, ay, bx, by);

        Set<Way> potentialWays = new HashSet<>();

        Collection<Node> allNodes = platform.getDataSet().getNodes();
        for (Node currentNode : allNodes) {
            if (platformBBox.bounds(currentNode.getBBox())) {
                List<OsmPrimitive> referrers = currentNode.getReferrers();
                for (OsmPrimitive referrer : referrers) {
                    if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
                        Way referrerWay = (Way) referrer;
                        if (this.ways.contains(referrerWay)) {
                            potentialWays.add(referrerWay);
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
        for (Way potentialWay : potentialWays) {
            double distance = this.calculateMinDistanceToSegment(platformNode, potentialWay);
            if (distance < minDistance) {
                minDistance = distance;
                nearestWay = potentialWay;
            }
        }

        return nearestWay;
    }

    /**
     * Calculates the minimum distance between a node and a way
     *
     * @param node node
     * @param way way
     * @return the minimum distance between a node and a way
     */
    private double calculateMinDistanceToSegment(Node node, Way way) {

        double minDistance = Double.MAX_VALUE;

        List<Pair<Node, Node>> waySegments = way.getNodePairs(false);

        for (Pair<Node, Node> waySegment : waySegments) {
            if (waySegment.a != node && waySegment.b != node) {
                double distanceToLine = this.calculateDistanceToSegment(node, waySegment);
                if (distanceToLine < minDistance) {
                    minDistance = distanceToLine;
                }
            }
        }

        return minDistance;

    }

    /**
     * Calculates the distance from point to segment and differentiates between
     * acute, right and obtuse triangles. If a triangle is acute or right, the
     * distance to segment is calculated as distance from point to line. If the
     * triangle is obtuse, the distance is calculated as the distance to the
     * nearest vertex of the segment.
     *
     * @param node node
     * @param segment segment
     * @return the distance from point to segment
     */
    private double calculateDistanceToSegment(Node node, Pair<Node, Node> segment) {

        if (node == segment.a || node == segment.b) {
            return 0.0;
        }

        double lengthA = node.getCoor().distance(segment.a.getCoor());
        double lengthB = node.getCoor().distance(segment.b.getCoor());
        double lengthC = segment.a.getCoor().distance(segment.b.getCoor());

        if (isObtuse(lengthC, lengthB, lengthA)) {
            return lengthB;
        }

        if (isObtuse(lengthA, lengthC, lengthB)) {
            return lengthA;
        }

        return calculateDistanceToLine(node, segment);
    }

    /**
     * Calculates the distance from point to line using formulas for triangle
     * area. Does not differentiate between acute, right and obtuse triangles
     *
     * @param node node
     * @param waySegment way segment
     * @return the distance from point to line
     */
    private double calculateDistanceToLine(Node node, Pair<Node, Node> segment) {

        /*
         * Let a be the triangle edge between the point and the first node of
         * the segment. Let b be the triangle edge between the point and the
         * second node of the segment. Let c be the triangle edge which is the
         * segment.
         */

        double lengthA = node.getCoor().distance(segment.a.getCoor());
        double lengthB = node.getCoor().distance(segment.b.getCoor());
        double lengthC = segment.a.getCoor().distance(segment.b.getCoor());

        // calculate triangle area using Heron's formula:
        double p = (lengthA + lengthB + lengthC) / 2.0;
        double triangleArea = Math.sqrt(p * (p - lengthA) * (p - lengthB) * (p - lengthC));

        // calculate the distance from point to segment using the 0.5*c*h
        // formula for triangle area:
        return triangleArea * 2.0 / lengthC;
    }

    /**
     * Checks if the angle opposite of the edge c is obtuse. Uses the cosine
     * theorem
     *
     * @param lengthA length A
     * @param lengthB length B
     * @param lengthC length C
     * @return true if the angle opposite of the edge c is obtuse
     */
    private boolean isObtuse(double lengthA, double lengthB, double lengthC) {

        /*-
         * Law of cosines:
         * c^2 = a^2 + b^2 - 2abcos
         * if c^2 = a^2 + b^2, it is a right triangle
         * if c^2 < a^2 + b^2, it is an acute triangle
         * if c^2 > a^2 + b^2, it is an obtuse triangle
         */

        if (lengthC * lengthC > lengthA * lengthA + lengthB * lengthB) {
            return true;
        }

        return false;
    }

    /**
     * Adds the given way to the map of assigned ways. Assumes that the given
     * way is not contained in the map.
     *
     * @param stop stop
     * @param way way
     */
    private void addAssignedWayToMap(PTStop stop, Way way) {
        if (stopToWay.containsKey(stop)) {
            List<Way> assignedWays = stopToWay.get(stop);
            assignedWays.add(way);
        } else {
            List<Way> assignedWays = new ArrayList<>();
            assignedWays.add(way);
            stopToWay.put(stop, assignedWays);
        }
    }

    /**
     * May be needed if the correspondence between stops and ways has changed
     * significantly
     */
    public static void reinitiate() {
        stopToWay = new HashMap<>();
    }

}
