// License: GPL. Copyright 2019 by Viesturs Zarins
package multipoly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;

/**
 * This class contains a collection of useful geometry functions.
 * @author viesturs
 *
 */
public class GeometryFunctions {
	public enum Intersection {INSIDE, OUTSIDE, CROSSING, EQUAL}
	
    /**
     * Finds the intersection of two lines of inifinite length.
     * @return EastNorth null if no intersection was found, the coordinates of the intersection otherwise
     */
    public static EastNorth getLineLineIntersection(EastNorth p1, EastNorth p2, EastNorth p3, EastNorth p4) {

        // Convert line from (point, point) form to ax+by=c
        double a1 = p2.getY() - p1.getY();
        double b1 = p1.getX() - p2.getX();
        double c1 = p2.getX() * p1.getY() - p1.getX() * p2.getY();

        double a2 = p4.getY() - p3.getY();
        double b2 = p3.getX() - p4.getX();
        double c2 = p4.getX() * p3.getY() - p3.getX() * p4.getY();

        // Solve the equations
        double det = a1 * b2 - a2 * b1;
        if (det == 0)
            return null; // Lines are parallel

        return new EastNorth((b1 * c2 - b2 * c1) / det, (a2 * c1 - a1 * c2) / det);
    }

    public static boolean segmentsParralel(EastNorth p1, EastNorth p2, EastNorth p3, EastNorth p4) {

        // Convert line from (point, point) form to ax+by=c
        double a1 = p2.getY() - p1.getY();
        double b1 = p1.getX() - p2.getX();

        double a2 = p4.getY() - p3.getY();
        double b2 = p3.getX() - p4.getX();

        // Solve the equations
        double det = a1 * b2 - a2 * b1;
        return Math.abs(det) < 1e-13;
    }

    /**
     * Calcualtes closest point to a line segment.
     * @param segmentP1
     * @param segmentP2
     * @param point
     * @return segmentP1 if it is the closest point, segmentP2 if it is the closest point,
     * a new point if closest point is between segmentP1 and segmentP2.
     */
    public static EastNorth closestPointToSegment(EastNorth segmentP1, EastNorth segmentP2, EastNorth point) {

        double ldx = segmentP2.getX() - segmentP1.getX();
        double ldy = segmentP2.getY() - segmentP1.getY();

        if (ldx == 0 && ldy == 0) //segment zero length
            return segmentP1;

        double pdx = point.getX() - segmentP1.getX();
        double pdy = point.getY() - segmentP1.getY();

        double offset = (pdx * ldx + pdy * ldy) / (ldx * ldx + ldy * ldy);

        if (offset <= 0)
            return segmentP1;
        else if (offset >= 1)
            return segmentP2;
        else
            return new EastNorth(segmentP1.getX() + ldx * offset, segmentP1.getY() + ldy * offset);

    }	
	
    /**
     * This method tests if secondNode is clockwise to first node.
     * @param commonNode starting point for both vectors
     * @param firstNode first vector end node
     * @param secondNode second vector end node
     * @return true if first vector is clockwise before second vector.
     */

    public static boolean angleIsClockwise(EastNorth commonNode, EastNorth firstNode, EastNorth secondNode) {
        double dy1 = (firstNode.getY() - commonNode.getY());
        double dy2 = (secondNode.getY() - commonNode.getY());
        double dx1 = (firstNode.getX() - commonNode.getX());
        double dx2 = (secondNode.getX() - commonNode.getX());

        return dy1 * dx2 - dx1 * dy2 > 0;
    }


    /**
     * Tests if two polygons instersect.
     * @param first
     * @param second
     * @return Inside if second is inside first, Outside, if second is outside first, Crossing, if they cross.
     */
    public static Intersection polygonIntersection(List<Node> outside, List<Node> inside) {
        Set<Node> outsideNodes = new HashSet<Node>(outside);
        
        boolean nodesInside = false;
        boolean nodesOutside = false;
        
        for (Node insideNode : inside) {
            if (!outsideNodes.contains(insideNode)) {
                if (nodeInsidePolygon(insideNode, outside)) {
                	nodesInside = true;
                }
                else {
                	nodesOutside = true;
                }
            }
        }

        if (nodesInside) {
        	if (nodesOutside){
        		return Intersection.CROSSING;
        	}
        	else {
        		return Intersection.INSIDE;
        	}
        }
        else {
        	if (nodesOutside){
        		return Intersection.OUTSIDE;
        	}
        	else {
        		return Intersection.EQUAL;
        	}
        }
    }

    /**
     * Tests if point is inside a polygon. The polygon can be self-intersecting. In such case the contains function works in xor-like manner.
     * @param polygonNodes list of nodes from polygon path.
     * @param point the point to test
     * @return true if the point is inside polygon.
     */
    public static boolean nodeInsidePolygon(Node point, List<Node> polygonNodes) {
        if (polygonNodes.size() < 2)
            return false;

        boolean inside = false;
        Node p1, p2;

        //iterate each side of the polygon, start with the last segment
        Node oldPoint = polygonNodes.get(polygonNodes.size() - 1);

        for (Node newPoint : polygonNodes) {
            //skip duplicate points
            if (newPoint.equals(oldPoint)) {
                continue;
            }

            //order points so p1.lat <= p2.lat;
            if (newPoint.getEastNorth().getY() > oldPoint.getEastNorth().getY()) {
                p1 = oldPoint;
                p2 = newPoint;
            } else {
                p1 = newPoint;
                p2 = oldPoint;
            }

            //test if the line is crossed and if so invert the inside flag.
            if ((newPoint.getEastNorth().getY() < point.getEastNorth().getY()) == (point.getEastNorth().getY() <= oldPoint.getEastNorth().getY())
                    && (point.getEastNorth().getX() - p1.getEastNorth().getX()) * (p2.getEastNorth().getY() - p1.getEastNorth().getY())
                    < (p2.getEastNorth().getX() - p1.getEastNorth().getX()) * (point.getEastNorth().getY() - p1.getEastNorth().getY()))
            {
                inside = !inside;
            }

            oldPoint = newPoint;
        }

        return inside;
    }


}
