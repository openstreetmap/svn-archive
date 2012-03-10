// License: GPL. Copyright 2011 by Ole Jørgen Brønner
package org.openstreetmap.josm.plugins.utilsplugin2.curves;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

public class CircleArcMaker {
    public static Collection<Command> doCircleArc(List<Node> selectedNodes, List<Way> selectedWays, int angleSeparation) {
        Collection<Command> cmds = new LinkedList<Command>();

        //// Decides which nodes to use as anchors based on selection
        /*
         * Rules goes like this:
         * If there are selected ways, at least one of these are used as target ways for the arc.
         * Selected ways override selected nodes. If nodes not part of the ways are selected they're ignored.
         *
         * When existing ways are reused for the arc, all ways overlapping these are transformed too.
         *
         * 1. Exactly 3 nodes selected:
         *      Use these nodes.
         *      - No way selected: create a new way.
         * 2. Exactly 1 node selected, node part of exactly 1 way with 3 or more nodes:
         *      Node selected used as first node, consequent nodes in the way's direction used as the rest.
         *      - Reversed if not enough nodes in forward direction
         *      - Selected node used as middle node its the middle node in a 3 node way
         *      - Parent way used
         */

        //// Anchor nodes
        Node n1 = null, n2 = null, n3 = null;

        if (false) {
            int nodeCount = selectedNodes.size();
            int wayCount = selectedWays.size();

            // TODO: filter garbage nodes based on selected ways

            // Never interested in more than 3 nodes. Nodes prioritized by reverse selection order, but keep their order.
            // TODO: replace by helper function (eg. getPostFixList(int count))
            Node[] nodesOfInterest = new Node[3];
            int nodesOfInterestCount = Math.min(nodeCount, 3);
            for (int i = nodesOfInterestCount - 1; i >= 0; i--) {
                nodesOfInterest[i] = selectedNodes.get(nodeCount - 1 - i);
            }
        }

        Set<Way> targetWays = new HashSet<Way>();

        boolean nodesHasBeenChoosen = false;
        if (selectedNodes.size() == 3) {
            Iterator<Node> nodeIter = selectedNodes.iterator();
            n1 = nodeIter.next();
            n2 = nodeIter.next();
            n3 = nodeIter.next();
            nodesHasBeenChoosen = true;
            if (selectedWays.isEmpty()) { // Create a brand new way
                Way newWay = new Way();
                targetWays.add(newWay);
                cmds.add(new AddCommand(newWay));
                newWay.addNode(n1);
                newWay.addNode(n2);
                newWay.addNode(n3);
            }
        }
        if (selectedWays.isEmpty() == false) {
            // TODO: use only two nodes inferring the orientation from the parent way.

            if (nodesHasBeenChoosen == false) {
                // Use the three last nodes in the way as anchors. This is intended to be used with the
                // built in draw mode
                Way w = selectedWays.iterator().next(); //TODO: select last selected way instead
                int nodeCount = w.getNodesCount();
                if (nodeCount < 3)
                    return null;
                n3 = w.getNode(nodeCount - 1);
                n2 = w.getNode(nodeCount - 2);
                n1 = w.getNode(nodeCount - 3);
                nodesHasBeenChoosen = true;
            }
            targetWays.addAll(OsmPrimitive.getFilteredList(n1.getReferrers(), Way.class));
            targetWays.addAll(OsmPrimitive.getFilteredList(n2.getReferrers(), Way.class));
            targetWays.addAll(OsmPrimitive.getFilteredList(n3.getReferrers(), Way.class));
            //            for(Way w : selectedWays) {
            //                targetWays.add(w);
            //            }

        }
        if (nodesHasBeenChoosen == false) {
            return null;
        }


        EastNorth p1 = n1.getEastNorth();
        EastNorth p2 = n2.getEastNorth();
        EastNorth p3 = n3.getEastNorth();
        // TODO: Check that the points are distinct

        // // Calculate the new points in the arc
        ReturnValue<Integer> p2Index = new ReturnValue<Integer>();
        List<EastNorth> points = circleArcPoints(p1, p2, p3, angleSeparation, false, p2Index);

        //// Create the new arc nodes. Insert anchor nodes at correct positions.
        List<Node> arcNodes = new ArrayList<Node>(points.size());
        arcNodes.add(n1);
        {
            int i = 1;
            for (EastNorth p : slice(points, 1, -2)) {
                //            if (p == p2) {
                if (i == p2Index.value) {
                    Node n2new = new Node(n2);
                    n2new.setEastNorth(p);
                    arcNodes.add(n2); // add the original n2, or else we can't find it in the target ways
                    cmds.add(new ChangeCommand(n2, n2new));
                } else {
                    Node n = new Node(p);
                    arcNodes.add(n);
                    cmds.add(new AddCommand(n));
                }
                i++;
            }
        }
        arcNodes.add(n3);

        Node[] anchorNodes = { n1, n2, n3 };
        //// "Fuse" the arc with all target ways
        fuseArc(anchorNodes, arcNodes, targetWays, cmds);

        return cmds;
    }

    private static void fuseArc(Node[] anchorNodes, List<Node> arcNodes, Set<Way> targetWays, Collection<Command> cmds) {

        for (Way originalTw : targetWays) {
            Way tw = new Way(originalTw);
            boolean didChangeTw = false;
            /// Do one segment at the time (so ways only sharing one segment is fused too)
            for (int a = 0; a < 2; a++) {
                int anchorBi = arcNodes.indexOf(anchorNodes[a]); // TODO: optimize away
                int anchorEi = arcNodes.indexOf(anchorNodes[a + 1]);
                /// Find the anchor node indices in current target way
                int bi = -1, ei = -1;
                int i = -1;
                // Caution: nodes might appear multiple times. For now only handle simple closed ways
                for (Node n : tw.getNodes()) {
                    i++;
                    // We look for the first anchor node. The next should be directly to the left or right.
                    // Exception when the way is closed
                    if (n == anchorNodes[a]) {
                        bi = i;
                        Node otherAnchor = anchorNodes[a + 1];
                        if (i > 0 && tw.getNode(i - 1) == otherAnchor) {
                            ei = i - 1;
                        } else if (i < (tw.getNodesCount() - 1) && tw.getNode(i + 1) == otherAnchor) {
                            ei = i + 1;
                        } else {
                            continue; // this can happen with closed ways. Continue searching for the correct index
                        }
                        break;
                    }
                }
                if (bi == -1 || ei == -1) {
                    continue; // this segment is not part of the target way
                }
                didChangeTw = true;

                /// Insert the nodes of this segment
                // Direction of target way relative to the arc node order
                int twDirection = ei > bi ? 1 : 0;
                int anchorI = anchorBi + 1; // don't insert the anchor nodes again
                int twI = bi + (twDirection == 1 ? 1 : 0); // TODO: explain
                while (anchorI < anchorEi) {
                    tw.addNode(twI, arcNodes.get(anchorI));
                    anchorI++;
                    twI += twDirection;
                }
            }
            if (didChangeTw)
                cmds.add(new ChangeCommand(originalTw, tw));
        }
    }

    /**
     * Return a list of coordinates lying an the circle arc determined by n1, n2 and n3.
     * The order of the list and which of the 3 possible arcs to construct are given by the order of n1, n2, n3
     *
     * @param includeAnchors include the anchorpoints in the list. The original objects will be used, not copies.
     *                       If {@code false}, p2 will be replaced by the closest arcpoint.
     * @param anchor2Index if non-null, it's value will be set to p2's index in the returned list.
     * @param angleSparation maximum angle separation between the arc points
     */
    private static List<EastNorth> circleArcPoints(EastNorth p1, EastNorth p2, EastNorth p3,
            int angleSeparation, boolean includeAnchors, ReturnValue<Integer> anchor2Index) {

        // triangle: three single nodes needed or a way with three nodes

        // let's get some shorter names
        double x1 = p1.east();
        double y1 = p1.north();
        double x2 = p2.east();
        double y2 = p2.north();
        double x3 = p3.east();
        double y3 = p3.north();

        // calculate the center (xc,yc)
        double s = 0.5 * ((x2 - x3) * (x1 - x3) - (y2 - y3) * (y3 - y1));
        double sUnder = (x1 - x2) * (y3 - y1) - (y2 - y1) * (x1 - x3);

        assert (sUnder == 0);

        s /= sUnder;

        double xc = 0.5 * (x1 + x2) + s * (y2 - y1);
        double yc = 0.5 * (y1 + y2) + s * (x1 - x2);

        // calculate the radius (r)
        double r = Math.sqrt(Math.pow(xc - x1, 2) + Math.pow(yc - y1, 2));

        // The angles of the anchor points relative to the center
        double realA1 = calcang(xc, yc, x1, y1);
        double realA2 = calcang(xc, yc, x2, y2);
        double realA3 = calcang(xc, yc, x3, y3);

        double startAngle = realA1;
        // Transform the angles to get a consistent starting point
        double a1 = 0;
        double a2 = normalizeAngle(realA2 - startAngle);
        double a3 = normalizeAngle(realA3 - startAngle);
        int direction = a3 > a2 ? 1 : -1;

        double radialLength = 0;
        if (direction == 1) { // counter clockwise
            radialLength = a3;
        } else { // clockwise
            radialLength = Math.PI * 2 - a3;
            // make the angles consistent with the direction.
            a2 = (Math.PI * 2 - a2);
            a3 = (Math.PI * 2 - a3);
        }
        int numberOfNodesInArc = Math.max((int) Math.ceil((radialLength / Math.PI) * 180 / angleSeparation)+1,
                3);
        List<EastNorth> points = new ArrayList<EastNorth>(numberOfNodesInArc);

        // Calculate the circle points in order
        double stepLength = radialLength / (numberOfNodesInArc-1);
        // Determine closest index to p2

        int indexJustBeforeP2 = (int) Math.floor(a2 / stepLength);
        int closestIndexToP2 = indexJustBeforeP2;
        if ((a2 - indexJustBeforeP2 * stepLength) > ((indexJustBeforeP2 + 1) * stepLength - a2)) {
            closestIndexToP2 = indexJustBeforeP2 + 1;
        }
        // can't merge with end node
        if (closestIndexToP2 == numberOfNodesInArc - 1) {
            closestIndexToP2--;
        } else if (closestIndexToP2 == 0) {
            closestIndexToP2++;
        }
        assert (closestIndexToP2 != 0);

        double a = direction * (stepLength);
        points.add(p1);
        if (indexJustBeforeP2 == 0 && includeAnchors) {
            points.add(p2);
        }
        // i is ahead of the real index by one, since we need to be ahead in the angle calculation
        for (int i = 2; i < numberOfNodesInArc; i++) {
            double nextA = direction * (i * stepLength);
            double realAngle = a + startAngle;
            double x = xc + r * Math.cos(realAngle);
            double y = yc + r * Math.sin(realAngle);

            points.add(new EastNorth(x, y));
            if (i - 1 == indexJustBeforeP2 && includeAnchors) {
                points.add(p2);
            }
            a = nextA;
        }
        points.add(p3);
        if (anchor2Index != null) {
            anchor2Index.value = closestIndexToP2;
        }
        return points;
    }

    // gah... why can't java support "reverse indices"?
    private static <T> List<T> slice(List<T> list, int from, int to) {
        if (to < 0)
            to += list.size() + 1;
        return list.subList(from, to);
    }

    /**
     * Normalizes {@code a} so it is between 0 and 2 PI
     */
    private static double normalizeAngle(double angle) {
        double PI2 = Math.PI * 2;
        if (angle < 0) {
            angle = angle + (Math.floor(-angle / PI2) + 1) * PI2;
        } else if (angle >= PI2) {
            angle = angle - Math.floor(angle / PI2) * PI2;
        }
        return angle;
    }

    private static double calcang(double xc, double yc, double x, double y) {
        // calculate the angle from xc|yc to x|y
        if (xc == x && yc == y)
            return 0; // actually invalid, but we won't have this case in this context
        double yd = Math.abs(y - yc);
        if (yd == 0 && xc < x)
            return 0;
        if (yd == 0 && xc > x)
            return Math.PI;
        double xd = Math.abs(x - xc);
        double a = Math.atan2(xd, yd);
        if (y > yc) {
            a = Math.PI - a;
        }
        if (x < xc) {
            a = -a;
        }
        a = 1.5 * Math.PI + a;
        if (a < 0) {
            a += 2 * Math.PI;
        }
        if (a >= 2 * Math.PI) {
            a -= 2 * Math.PI;
        }
        return a;
    }
    public static class ReturnValue<T> {
        public T value;
    }
}
