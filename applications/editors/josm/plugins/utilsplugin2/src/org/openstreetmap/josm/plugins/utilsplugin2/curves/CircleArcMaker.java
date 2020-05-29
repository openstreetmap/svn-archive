// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.curves;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.JosmRuntimeException;

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
        if (radiusInMeters < 0.01)
            throw new JosmRuntimeException(tr("Radius too small"));

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
            if (w.isClosed()) {
                // see #19188
                nodes.clear();
                nodes.addAll(findShortestPart(w, anchorNodes));
            }
            // Fix #7341. sort nodes in ways nodes order
            List<Node> consideredNodes = Arrays.asList(n1, n2, n3);
            consideredNodes.sort((o1, o2) -> nodes.indexOf(o1) - nodes.indexOf(o2));
            n1 = consideredNodes.get(0);
            n2 = consideredNodes.get(1);
            n3 = consideredNodes.get(2);
            anchorNodes = Arrays.asList(n1, n2, n3);
        }


        List<Node> cwTest = new ArrayList<>(Arrays.asList(n1, n2, n3));
        if (cwTest.get(0) != cwTest.get(cwTest.size() - 1)) {
            cwTest.add(cwTest.get(0));
        }
        boolean clockwise = Geometry.isClockwise(cwTest);

        boolean needsUndo = false;
        if (!cmds.isEmpty()) {
            UndoRedoHandler.getInstance().add(new SequenceCommand("add nodes", cmds));
            needsUndo = true;
        }

        int pos1 = nodes.indexOf(n1);
        int pos3 = nodes.indexOf(n3);
        Set<Node> fixNodes = new HashSet<>(anchorNodes);
        if (!selectedWays.isEmpty()) {
            for (int i = pos1 + 1; i < pos3; i++) {
                Node n = nodes.get(i);
                if (n.isTagged() || n.isReferredByWays(2) || n.referrers(Relation.class).count() > 0) {
                    fixNodes.add(n);
                }
            }
        }

        List<Node> orig = nodes.subList(pos1, pos3 + 1);
        List<Node> arcNodes = new ArrayList<>(orig);

        Set<Way> targetWays = new HashSet<>();
        if (!selectedWays.isEmpty()) {
            orig.forEach(n -> targetWays.addAll(n.getParentWays()));
        } else {
            targetWays.add(w);
        }
        try {
            List<Command> modCmds = worker(arcNodes, fixNodes, center, radius, maxAngle, clockwise);
            cmds.addAll(modCmds);
            if (!arcNodes.equals(orig)) {
                fuseArc(ds, arcNodes, targetWays, cmds);
            }
        } finally {
            if (needsUndo) {
                // make sure we don't add the new nodes twice
                UndoRedoHandler.getInstance().undo(1);
            }
        }
        if (cmds.isEmpty()) {
            throw new JosmRuntimeException(tr("Nothing to do"));
        }
        return cmds;
    }

    /**
     * Try to find out which nodes should be moved when a closed way is modified. The positions of the anchor
     * nodes might not give the right order. Rotate the nodes so that each of the selected nodes is first
     * and check which variant produces the shortest part of the way.
     * @param w the closed way to modify
     * @param anchorNodes the (selected) anchor nodes
     * @return the way nodes, possibly rotated
     * @throws JosmRuntimeException if no usable rotation was found
     */
    private static List<Node> findShortestPart(Way w, List<Node> anchorNodes) {
        int bestRotate = 0;
        double shortest = Double.MAX_VALUE;
        final double wayLength = w.getLength();
        for (int i = 0; i < w.getNodesCount() - 1; i++) {
            List<Node> nodes = rotate(w, i);
            List<Integer> positions = anchorNodes.stream().map(nodes::indexOf).sorted().collect(Collectors.toList());
            double lenghth = getLength(nodes, positions.get(0), positions.get(2));
            if (lenghth < shortest) {
                bestRotate = i;
                shortest = lenghth;
            }
        }
        if (shortest >= wayLength / 2)
            throw new JosmRuntimeException(tr("Could not detect which part of the closed way should be changed"));
        return rotate(w, bestRotate);
    }

    private static List<Node> rotate(Way w, int distance) {
        List<Node> nodes = new ArrayList<>(w.getNodes());
        nodes.remove(nodes.size() - 1); // remove closing node
        Collections.rotate(nodes, distance);
        nodes.add(nodes.get(0));
        return nodes;
    }

    private static double getLength(List<Node> nodes, int pos1, int pos3) {
        Way tmp = new Way();
        tmp.setNodes(nodes.subList(pos1, pos3+1));
        return tmp.getLength();
    }

    // code partly taken from AlignInCircleAction
    private static List<Command> worker(List<Node> origNodes, Set<Node> origFixNodes, EastNorth center, double radius,
            double maxAngle, boolean clockwise) {
        List<Command> cmds = new LinkedList<>();
        // Move each node to that distance from the center.
        // Nodes that are not "fix" will be adjusted making regular arcs.
        List<Node> nodes = new ArrayList<>(origNodes);
        Set<Node> fixNodes = new HashSet<>(origFixNodes);
        double maxStep = Math.PI * 2 / (360.0 / maxAngle);
        double sumAbsDelta = 0;
        int i = 0; // Start position for current arc
        int j; // End position for current arc
        while (i < nodes.size()) {
            Node first = nodes.get(i);
            PolarCoor pcFirst = new PolarCoor(radius, PolarCoor.computeAngle(first.getEastNorth(), center), center);
            for (j = i + 1; j < nodes.size(); j++) {
                Node test = nodes.get(j);
                if (fixNodes.contains(test)) {
                    break;
                }
            }

            if (j < nodes.size()) {
                Node last = nodes.get(j);
                PolarCoor pcLast = new PolarCoor(last.getEastNorth(), center);
                double delta = pcLast.angle - pcFirst.angle;
                if (((!clockwise && delta < 0) || (clockwise && delta > 0))
                        && Math.signum(pcFirst.angle) == Math.signum(pcLast.angle)) {
                    // cannot project node onto circle arc, ignore that it is fixed
                    if (!last.isSelected() && fixNodes.remove(last)) {
                        continue;
                    }
                    if (!first.isSelected() && fixNodes.remove(first)) {
                        // try again with fewer fixed nodes
                        return worker(origNodes, fixNodes, center, radius, maxAngle, clockwise);
                    }
                }
                if (!clockwise && delta < 0) {
                    delta += 2 * Math.PI;
                } else if (clockwise && delta > 0) {
                    delta -= 2 * Math.PI;
                }
                sumAbsDelta += Math.abs(delta);
                if (sumAbsDelta > 2 * Math.PI) {
                    // something went really wrong, we would add more than a full circle
                    throw new JosmRuntimeException("Would create a loop");
                }

                // do we have enough nodes to produce a nice circle?
                int numToAdd = Math.max((int) Math.ceil(Math.abs(delta / maxStep)), j - i) - (j - i);
                int added = 0;
                double step = delta / (numToAdd + j - i);

                // move existing nodes or add new nodes
                List<Node> oldNodes = new ArrayList<>(nodes.subList(i, j));
                PolarCoor ref = pcFirst;
                for (int k = i; k < j + numToAdd; k++) {
                    PolarCoor pc1 = new PolarCoor(radius, pcFirst.angle + (k - i) * step, center);
                    if (!oldNodes.isEmpty()) {
                        PolarCoor pc2 = new PolarCoor(oldNodes.get(0).getEastNorth(), center);
                        if ((pc2.angle < ref.angle && ref.angle < 0 && step > 0)
                                || (pc2.angle > ref.angle && ref.angle > 0 && step < 0)) {
                            // projected node would produce a loop
                            pc2 = ref; //
                        }

                        double delta2 = ref.angle - pc2.angle;
                        if (ref.angle < 0 && pc2.angle > 0) {
                            delta2 += 2 * Math.PI;
                        } else if (ref.angle > 0 && pc2.angle < 0) {
                            delta2 -= 2 * Math.PI;
                        }
                        if (added >= numToAdd || Math.abs(delta2) < Math.abs(step * 1.5)) {
                            // existing node is close enough
                            addMoveCommandIfNeeded(oldNodes.remove(0), pc1, cmds);
                            ref = pc1;
                        }
                    }
                    if (ref != pc1) {
                        ref = pc1;
                        Node nNew = new Node(pc1.toEastNorth());
                        nodes.add(k, nNew);
                        cmds.add(new AddCommand(nodes.get(0).getDataSet(), nNew));
                        ++added;
                    }
                }

                j += added;
            }
            i = j; // Update start point for next iteration
        }
        origNodes.clear();
        origNodes.addAll(nodes);
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

    private static void fuseArc(DataSet ds, List<Node> arcNodes, Set<Way> targetWays, Collection<Command> cmds) {
        // replace each segment of the target ways with the corresponding nodes of the new arc
        for (Way originalTw : targetWays) {
            Way tw = new Way(originalTw);
            boolean twWasChanged = false;
            for (int i = 0; i < arcNodes.size(); i++) {
                Node arcNode1 = arcNodes.get(i);
                // we don't want to match nodes which where added by worker
                if (arcNode1.getDataSet() != ds || !arcNode1.getParentWays().contains(originalTw))
                    continue;

                boolean changed = false;
                for (int j = i + 1; j < arcNodes.size() && !changed; j++) {
                    Node arcNode2 = arcNodes.get(j);
                    if (arcNode2.getDataSet() != ds || !arcNode2.getParentWays().contains(originalTw))
                        continue;
                    changed = tryAddArc(tw, i, j, arcNodes);
                    twWasChanged |= changed;
                }
            }
            if (twWasChanged) {
                cmds.add(new ChangeCommand(ds, originalTw, tw));
            }
        }
    }

    private static boolean tryAddArc(Way tw, int i, int j, List<Node> arcNodes) {
        int pos1 = tw.getNodes().indexOf(arcNodes.get(i));
        int pos2 = tw.getNodes().indexOf(arcNodes.get(j));
        if (tw.isClosed()) {
            if (pos1 - pos2 > 1 && pos2 == 0) {
                pos2 = tw.getNodesCount() - 1;
            } else if (pos2 - pos1 > 1 && pos1 == 0) {
                pos1 = tw.getNodesCount() - 1;
            }
        }
        if (pos2 + 1 == pos1) {
            for (int k = i + 1; k < j; k++) {
                tw.addNode(pos1, arcNodes.get(k));
            }
            return true;
        } else if (pos2 - 1 == pos1) {
            for (int k = j - 1; k > i; k--) {
                tw.addNode(pos2, arcNodes.get(k));
            }
            return true;
        }
        return false;
    }
}
