// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.curves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.PolarCoor;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Geometry;

/**
 * Create a circle arc
 */
public final class CircleArcMaker {

    private CircleArcMaker() {
        // Hide default constructor for utilities classes
    }

    public static Collection<Command> doCircleArc(List<Node> selectedNodes, List<Way> selectedWays) {
        List<Command> cmds = new LinkedList<>();

        //// Decides which nodes to use as anchors based on selection
        /*
         * Rules goes like this:
         * If there are selected ways, at least one of these are used as target ways for the arc.
         * Selected ways override selected nodes. If nodes not part of the ways are selected they're ignored.
         *
         * When existing ways are reused for the arc, all ways overlapping these are transformed too.
         *
         * Exactly 3 nodes have to be selected.
         *      - No way selected: create a new way.
         */

        //// Anchor nodes
        Node n1 = null, n2 = null, n3 = null;

        DataSet ds = MainApplication.getLayerManager().getEditDataSet();

        if (selectedWays.size() > 1)
             return cmds;

        Way w = null;
        boolean nodesHaveBeenChoosen = false;
        if (selectedNodes.size() == 3) {
            Iterator<Node> nodeIter = selectedNodes.iterator();
            n1 = nodeIter.next();
            n2 = nodeIter.next();
            n3 = nodeIter.next();
            nodesHaveBeenChoosen = true;
        }
        if (!selectedWays.isEmpty()) {
            w = selectedWays.iterator().next();
            if (!nodesHaveBeenChoosen) {
                // Use the three last nodes in the way as anchors. This is intended to be used with the
                // built in draw mode
                int nodeCount = w.getNodesCount();
                if (nodeCount < 3)
                    return cmds;
                n3 = w.getNode(nodeCount - 1);
                n2 = w.getNode(nodeCount - 2);
                n1 = w.getNode(nodeCount - 3);
                nodesHaveBeenChoosen = true;
            }
        }
        List<Node> anchorNodes = Arrays.asList(n1, n2, n3);
        if (!nodesHaveBeenChoosen || (w != null && !w.getNodes().containsAll(anchorNodes))) {
            return cmds;
        }

        EastNorth p1 = n1.getEastNorth();
        EastNorth p2 = n2.getEastNorth();
        EastNorth p3 = n3.getEastNorth();

        // make sure that points are different
        if (p1.equals(p2) || p1.equals(p3) || p2.equals(p3)) {
            return cmds;
        }

        EastNorth center = Geometry.getCenter(anchorNodes);
        if (center == null)
            return cmds;
        double radius = center.distance(p1);

        // see #10777: calculate reasonable number of nodes for full circle (copy from CreateCircleAction)
        LatLon ll1 = ProjectionRegistry.getProjection().eastNorth2latlon(p1);
        LatLon ll2 = ProjectionRegistry.getProjection().eastNorth2latlon(center);

        double radiusInMeters = ll1.greatCircleDistance(ll2);

        int numberOfNodesInCircle = (int) Math.ceil(6.0 * Math.pow(radiusInMeters, 0.5));
        // an odd number of nodes makes the distribution uneven
        if (numberOfNodesInCircle < 6) {
            numberOfNodesInCircle = 6;
        } else if ((numberOfNodesInCircle % 2) != 0) {
            // add 1 to make it even
            numberOfNodesInCircle++;
        }
        double maxAngle = 360.0 / numberOfNodesInCircle;

        if (w == null) {
            w = new Way();
            w.setNodes(anchorNodes);
            cmds.add(new AddCommand(ds, w));
        }
        final List<Node> nodes = new ArrayList<>(w.getNodes());

        if (!selectedWays.isEmpty()) {
            // Fix #7341. sort nodes in ways nodes order
            List<Node> consideredNodes = Arrays.asList(n1, n2, n3);
            Collections.sort(consideredNodes, (o1, o2) -> nodes.indexOf(o1) - nodes.indexOf(o2));
            n1 = consideredNodes.get(0);
            n3 = consideredNodes.get(2);
        }

        Set<Node> fixNodes = new HashSet<>(anchorNodes);
        if (!selectedWays.isEmpty()) {
            nodes.stream().filter(n -> n.getParentWays().size() > 1).forEach(fixNodes::add);
        }
        boolean needsUndo = false;
        if (!cmds.isEmpty()) {
            UndoRedoHandler.getInstance().add(new SequenceCommand("add nodes", cmds));
            needsUndo = true;
        }

        int pos1 = nodes.indexOf(n1);
        int pos3 = nodes.indexOf(n3);
        List<Node> toModify = new ArrayList<>(nodes.subList(pos1, pos3 + 1));
        cmds.addAll(worker(toModify, fixNodes, center, radius, maxAngle));
        if (toModify.size() > pos3 + 1 - pos1) {
            List<Node> changed = new ArrayList<>();
            changed.addAll(nodes.subList(0, pos1));
            changed.addAll(toModify);
            changed.addAll(nodes.subList(pos3 + 1, nodes.size()));
            Way wNew = new Way(w);
            wNew.setNodes(changed);
            cmds.add(new ChangeCommand(w, wNew));
        }
        if (needsUndo) {
            // make sure we don't add the new nodes twice
            UndoRedoHandler.getInstance().undo(1);
        }
        return cmds;
    }

    // code partly taken from AlignInCircleAction
    private static List<Command> worker(List<Node> nodes, Set<Node> fixNodes, EastNorth center, double radius, double maxAngle) {
        List<Command> cmds = new LinkedList<>();

        // Move each node to that distance from the center.
        // Nodes that are not "fix" will be adjust making regular arcs.
        int nodeCount = nodes.size();

        List<Node> cwTest = new ArrayList<>(nodes);
        if (cwTest.get(0) != cwTest.get(cwTest.size() - 1)) {
            cwTest.add(cwTest.get(0));
        }
        boolean clockWise = Geometry.isClockwise(cwTest);
        double maxStep = Math.PI * 2 / (360.0 / maxAngle);

        // Search first fixed node
        int startPosition;
        for (startPosition = 0; startPosition < nodeCount; startPosition++) {
            if (fixNodes.contains(nodes.get(startPosition)))
                break;
        }
        int i = startPosition; // Start position for current arc
        int j; // End position for current arc
        while (i < nodeCount) {
            for (j = i + 1; j < nodeCount; j++) {
                if (fixNodes.contains(nodes.get(j)))
                    break;
            }
            Node first = nodes.get(i);

            PolarCoor pcFirst = new PolarCoor(radius, PolarCoor.computeAngle(first.getEastNorth(), center), center);
            addMoveCommandIfNeeded(first, pcFirst, cmds);
            if (j < nodeCount) {
                double delta;
                PolarCoor pcLast = new PolarCoor(nodes.get(j).getEastNorth(), center);
                delta = pcLast.angle - pcFirst.angle;
                if (!clockWise && delta < 0) {
                    delta += 2 * Math.PI;
                } else if (clockWise && delta > 0) {
                    delta -= 2 * Math.PI;
                }
                // do we have enough nodes to produce a nice circle?
                int numToAdd = Math.max((int) Math.ceil(Math.abs(delta / maxStep)), j - i) - (j-i);
                double step = delta / (numToAdd + j - i);
                for (int k = i + 1; k < j; k++) {
                    PolarCoor p = new PolarCoor(radius, pcFirst.angle + (k - i) * step, center);
                    addMoveCommandIfNeeded(nodes.get(k), p, cmds);
                }
                // add needed nodes
                for (int k = j; k < j + numToAdd; k++) {
                    PolarCoor p = new PolarCoor(radius, pcFirst.angle + (k - i) * step, center);
                    Node nNew = new Node(p.toEastNorth());
                    nodes.add(k, nNew);
                    cmds.add(new AddCommand(nodes.get(0).getDataSet(), nNew));
                }
                j += numToAdd;
                nodeCount += numToAdd;
            }
            i = j; // Update start point for next iteration
        }
        return cmds;
    }

    private static void addMoveCommandIfNeeded(Node n, PolarCoor coor, Collection<Command> cmds) {
        EastNorth en = coor.toEastNorth();
        double deltaEast = en.east() - n.getEastNorth().east();
        double deltaNorth = en.north() - n.getEastNorth().north();
        if (Math.abs(deltaEast) > 1e-7 || Math.abs(deltaNorth) > 1e-7) {
            cmds.add(new MoveCommand(n, deltaEast, deltaNorth));
        }
    }

}
