// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;

import edu.princeton.cs.algs4.AssignmentProblem;

/**
 * Utilities for Replace Geometry
 * @author joshdoe
 */
public final class ReplaceGeometryUtils {

    private ReplaceGeometryUtils() {
        // Hide default constructor for utilities classes
    }

    /**
     * Replace new or uploaded object with new object
     * @return (in case of success) a command to update the geometry of one primitive and remove the other
     */
    public static ReplaceGeometryCommand buildReplaceWithNewCommand(OsmPrimitive firstObject, OsmPrimitive secondObject) {
        if (firstObject instanceof Node && secondObject instanceof Node) {
            return buildReplaceNodeWithNewCommand((Node) firstObject, (Node) secondObject);
        } else if (firstObject instanceof Way && secondObject instanceof Way) {
            return buildReplaceWayWithNewCommand(Arrays.asList((Way) firstObject, (Way) secondObject));
        } else if (firstObject instanceof Node) {
            return buildUpgradeNodeCommand((Node) firstObject, secondObject);
        } else if (secondObject instanceof Node) {
            return buildUpgradeNodeCommand((Node) secondObject, firstObject);
        } else {
            throw new IllegalArgumentException(
                    tr("This tool can only replace a node, upgrade a node to a way or a multipolygon, or replace a way with a way."));
        }
    }

    /**
     * Replace subjectObject geometry with referenceObject geometry and merge tags
     * and relation memberships.
     * @param subjectObject object to modify
     * @param referenceSubject object that gives new geometry and is removed
     * @return (in case of success) a command to update the geometry of fist object and remove the other
     */
    public static ReplaceGeometryCommand buildReplaceCommand(OsmPrimitive subjectObject, OsmPrimitive referenceSubject) {
        if (subjectObject instanceof Node && referenceSubject instanceof Node) {
            return buildReplaceNodeCommand((Node) subjectObject, (Node) referenceSubject);
        } else if (subjectObject instanceof Way && referenceSubject instanceof Way) {
            return buildReplaceWayCommand((Way) subjectObject, (Way) referenceSubject);
        } else if (subjectObject instanceof Node) {
            return buildUpgradeNodeCommand((Node) subjectObject, referenceSubject);
        } else if (referenceSubject instanceof Node) {
            // TODO: fix this illogical reversal?
            return buildUpgradeNodeCommand((Node) referenceSubject, subjectObject);
        } else {
            throw new IllegalArgumentException(
                    tr("This tool can only replace a node, upgrade a node to a way or a multipolygon, or replace a way with a way."));
        }
    }

    /**
     * Replace a new or uploaded node with a new node
     * @return (in case of success) a command to update the geometry of one primitive and remove the other
     */
    public static ReplaceGeometryCommand buildReplaceNodeWithNewCommand(Node firstNode, Node secondNode) {
        if (firstNode.isNew() && !secondNode.isNew())
            return buildReplaceNodeCommand(secondNode, firstNode);
        else if (!firstNode.isNew() && secondNode.isNew())
            return buildReplaceNodeCommand(firstNode, secondNode);
        else
            // both nodes are new OR uploaded, act like MergeNodes, moving first
            // node to second
            return buildReplaceNodeCommand(firstNode, secondNode);
    }

    /**
     * Replace a node with another node (similar to MergeNodesAction)
     */
    public static ReplaceGeometryCommand buildReplaceNodeCommand(Node subjectNode, Node referenceNode) {
        if (!subjectNode.getParentWays().isEmpty()) {
            throw new ReplaceGeometryException(tr("Node belongs to way(s), cannot replace."));
        }
        // FIXME: handle different layers
        List<Command> commands = new ArrayList<>();
        Command c = MergeNodesAction.mergeNodes(
            Arrays.asList(subjectNode, referenceNode), referenceNode);
        if (c == null) {
            // User cancelled
            return null;
        }
        commands.add(c);

        return new ReplaceGeometryCommand(
                tr("Replace geometry for node {0}", subjectNode.getDisplayName(DefaultNameFormatter.getInstance())),
                commands);
    }

    /**
     * Upgrade a node to a way or multipolygon
     *
     * @param subjectNode node to be replaced
     * @param referenceObject object with greater spatial quality
     */
    public static ReplaceGeometryCommand buildUpgradeNodeCommand(Node subjectNode, OsmPrimitive referenceObject) {
        if (!subjectNode.getParentWays().isEmpty()) {
            throw new ReplaceGeometryException(tr("Node belongs to way(s), cannot replace."));
        }

        if (referenceObject instanceof Relation && !((Relation) referenceObject).isMultipolygon()) {
            throw new ReplaceGeometryException(tr("Relation is not a multipolygon, cannot be used as a replacement."));
        }

        Node nodeToReplace = null;
        // see if we need to replace a node in the replacement way to preserve connection in history
        if (!subjectNode.isNew()) {
            // Prepare a list of nodes that are not important
            Collection<Node> nodePool = new HashSet<>();
            if (referenceObject instanceof Way) {
                nodePool.addAll(getUnimportantNodes((Way) referenceObject));
            } else if (referenceObject instanceof Relation) {
                for (RelationMember member : ((Relation) referenceObject).getMembers()) {
                    if ((member.getRole().equals("outer") || member.getRole().equals("inner"))
                            && member.isWay()) {
                        // TODO: could consider more nodes, such as nodes that are members of other ways,
                        // just need to replace occurences in all referrers
                        nodePool.addAll(getUnimportantNodes(member.getWay()));
                    }
                }
            } else {
                assert false;
            }
            nodeToReplace = findNearestNode(subjectNode, nodePool);
        }

        List<Command> commands = new ArrayList<>();
        AbstractMap<String, String> nodeTags = subjectNode.getKeys();

        // merge tags
        try {
            commands.addAll(getTagConflictResolutionCommands(subjectNode, referenceObject));
        } catch (UserCancelException e) {
            // user cancelled tag merge dialog
            return null;
        }

        // replace sacrificial node in way with node that is being upgraded
        if (nodeToReplace != null) {
            // node should only have one parent, a way
            Way parentWay = (Way) nodeToReplace.getReferrers().get(0);
            List<Node> wayNodes = parentWay.getNodes();
            int idx = wayNodes.indexOf(nodeToReplace);
            wayNodes.set(idx, subjectNode);
            if (idx == 0 && parentWay.isClosed()) {
                // node is at start/end of way
                wayNodes.set(wayNodes.size() - 1, subjectNode);
            }
            commands.add(new ChangeNodesCommand(parentWay, wayNodes));
            commands.add(new MoveCommand(subjectNode, nodeToReplace.getCoor()));
            commands.add(new DeleteCommand(nodeToReplace));

            // delete tags from node
            if (!nodeTags.isEmpty()) {
                for (String key : nodeTags.keySet()) {
                    commands.add(new ChangePropertyCommand(subjectNode, key, null));
                }

            }
        } else {
            // no node to replace, so just delete the original node
            commands.add(new DeleteCommand(subjectNode));
        }

        MainApplication.getLayerManager().getEditDataSet().setSelected(referenceObject);

        return new ReplaceGeometryCommand(
                tr("Replace geometry for node {0}", subjectNode.getDisplayName(DefaultNameFormatter.getInstance())),
                commands);
    }

    public static ReplaceGeometryCommand buildReplaceWayWithNewCommand(List<Way> selection) {
        // determine which way will be replaced and which will provide the geometry
        boolean overrideNewCheck = false;
        int idxNew = selection.get(0).isNew() ? 0 : 1;
        if (selection.get(1-idxNew).isNew()) {
            // compute the nodes which are not shared by both ways
            Set<Node> s0OnlyNodes = getDistinctNodes(selection.get(0), selection.get(1));
            Set<Node> s1OnlyNodes = getDistinctNodes(selection.get(1), selection.get(0));

            boolean hasNewS0 = s0OnlyNodes.stream().anyMatch(Node::isNew);
            boolean hasNewS1 = s1OnlyNodes.stream().anyMatch(Node::isNew);
            if (hasNewS0 != hasNewS1) {
                // OK: one way doesn't have new nodes which don't appear in both ways
                overrideNewCheck = true;
                if (hasNewS1) {
                    idxNew = 1;
                }
            }
        }
        Way referenceWay = selection.get(idxNew);
        Way subjectWay = selection.get(1 - idxNew);

        if (!overrideNewCheck && (subjectWay.isNew() || !referenceWay.isNew())) {
            throw new ReplaceGeometryException(
                    tr("Both ways are new and have new nodes, cannot decide which one has the correct geometry."));
        }
        return buildReplaceWayCommand(subjectWay, referenceWay);
    }

    /**
     * Replace geometry of subjectWay by that of referenceWay. Tries to keep the history of nodes.
     * @param subjectWay way to modify
     * @param referenceWay way to remove
     * @return
     */
    public static ReplaceGeometryCommand buildReplaceWayCommand(Way subjectWay, Way referenceWay) {

        Area a = MainApplication.getLayerManager().getEditDataSet().getDataSourceArea();
        if (!isInArea(subjectWay, a) || !isInArea(referenceWay, a)) {
            throw new ReplaceGeometryException(tr("The ways must be entirely within the downloaded area."));
        }

        if (hasImportantNode(referenceWay, subjectWay)) {
            throw new ReplaceGeometryException(
                    tr("The way to be replaced cannot have any nodes with properties or relation memberships unless they belong to both ways."));
        }

        List<Command> commands = new ArrayList<>();

        // merge tags
        try {
            commands.addAll(getTagConflictResolutionCommands(referenceWay, subjectWay));
        } catch (UserCancelException e) {
            // user cancelled tag merge dialog
            Logging.trace(e);
            return null;
        }

        // Prepare a list of nodes that are not used anywhere except in the way
        List<Node> nodePool = getUnimportantNodes(subjectWay);

        // And the same for geometry, list nodes that can be freely deleted
        List<Node> geometryPool = new LinkedList<>();
        for (Node node : referenceWay.getNodes()) {
            List<OsmPrimitive> referrers = node.getReferrers();
            if (node.isNew() && !node.isDeleted() && referrers.size() == 1
                    && referrers.get(0).equals(referenceWay) && !subjectWay.containsNode(node)
                    && !hasInterestingKey(node) && !geometryPool.contains(node))
                geometryPool.add(node);
        }

        int gLen = geometryPool.size();
        int nLen = nodePool.size();
        int N = Math.max(gLen, nLen);
        boolean useRobust = Config.getPref().getBoolean("utilsplugin2.replace-geometry.robustAssignment", true)
                && N <= Config.getPref().getInt("utilsplugin2.replace-geometry.robustAssignment.max-size", 300);

        // Find new nodes that are closest to the old ones, remove matching old ones from the pool
        // Assign node moves with least overall distance moved
        Map<Node, Node> nodeAssoc = new HashMap<>();
        if (gLen > 0 && nLen > 0) {
            if (useRobust) {  // use robust, but slower assignment
                double[][] cost = new double[N][N];
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        cost[i][j] = Double.MAX_VALUE;
                    }
                }

                double maxDistance = Double.parseDouble(Config.getPref().get("utilsplugin2.replace-geometry.max-distance", "1"));
                for (int i = 0; i < nLen; i++) {
                    for (int j = 0; j < gLen; j++) {
                        double d = nodePool.get(i).getCoor().distance(geometryPool.get(j).getCoor());
                        if (d > maxDistance) {
                            cost[i][j] = Double.MAX_VALUE;
                        } else {
                            cost[i][j] = d;
                        }
                    }
                }
                AssignmentProblem assignment;
                try {
                    assignment = new AssignmentProblem(cost);
                    for (int i = 0; i < N; i++) {
                        int nIdx = i;
                        int gIdx = assignment.sol(i);
                        if (cost[nIdx][gIdx] != Double.MAX_VALUE) {
                            nodeAssoc.put(geometryPool.get(gIdx), nodePool.get(nIdx));
                        }
                    }
                    // node will be moved, remove from pool
                    for (Node n : nodeAssoc.values()) {
                        nodePool.remove(n);
                    }
                } catch (Exception e) {
                    useRobust = false;
                    new Notification(
                            tr("Exceeded iteration limit for robust method, using simpler method.")
                            ).setIcon(JOptionPane.WARNING_MESSAGE).show();
                    nodeAssoc = new HashMap<>();
                }
            }
            if (!useRobust) { // use simple, faster, but less robust assignment method
                for (Node n : geometryPool) {
                    Node nearest = findNearestNode(n, nodePool);
                    if (nearest != null) {
                        nodeAssoc.put(n, nearest);
                        nodePool.remove(nearest);
                    }
                }

            }
        }

        // Now that we have replacement list, move all unused new nodes to nodePool (and delete them afterwards)
        for (Node n : geometryPool) {
            if (nodeAssoc.containsKey(n))
                nodePool.add(n);
        }

        // And prepare a list of nodes with all the replacements
        List<Node> geometryNodes = referenceWay.getNodes();
        for (int i = 0; i < geometryNodes.size(); i++) {
            if (nodeAssoc.containsKey(geometryNodes.get(i)))
                geometryNodes.set(i, nodeAssoc.get(geometryNodes.get(i)));
        }

        // Now do the replacement
        commands.add(new ChangeNodesCommand(subjectWay, geometryNodes));

        // Move old nodes to new positions
        for (Node node : nodeAssoc.keySet()) {
            commands.add(new MoveCommand(nodeAssoc.get(node), node.getCoor()));
        }

        // Remove geometry way from selection
        MainApplication.getLayerManager().getEditDataSet().clearSelection(referenceWay);

        // And delete old geometry way
        commands.add(new DeleteCommand(referenceWay));

        // Delete nodes that are not used anymore
        if (!nodePool.isEmpty())
            commands.add(new DeleteCommand(nodePool));

        // Two items in undo stack: change original way and delete geometry way
        return new ReplaceGeometryCommand(
                tr("Replace geometry for way {0}", subjectWay.getDisplayName(DefaultNameFormatter.getInstance())),
                commands);
    }

    /**
     * Create a list of distinct nodes that are not tagged and not used anywhere except in the way.
     * @param way the way
     * @return list of distinct nodes that are not tagged and not used anywhere except in the way
     */
    protected static List<Node> getUnimportantNodes(Way way) {
        List<Node> nodePool = new LinkedList<>();
        for (Node n : way.getNodes()) {
            List<OsmPrimitive> referrers = n.getReferrers();
            if (!n.isDeleted() && referrers.size() == 1 && referrers.get(0).equals(way)
                    && !hasInterestingKey(n) && !nodePool.contains(n)) {
                nodePool.add(n);
            }
        }
        return nodePool;
    }

    /**
     * Checks if a way has at least one important node (e.g. interesting tag,
     * role membership), and thus cannot be safely modified.
     */
    protected static boolean hasImportantNode(Way geometry, Way way) {
        for (Node n : way.getNodes()) {
            // if original and replacement way share a node, it's safe to replace
            if (geometry.containsNode(n)) {
                continue;
            }
            //TODO: if way is connected to other ways, warn or disallow?
            for (OsmPrimitive o : n.getReferrers()) {
                if (o instanceof Relation) {
                    return true;
                }
            }
            if (hasInterestingKey(n)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean hasInterestingKey(OsmPrimitive object) {
        for (String key : object.getKeys().keySet()) {
            if (!OsmPrimitive.isUninterestingKey(key)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isInArea(Node node, Area area) {
        LatLon ll = node.getCoor();
        if (node.isNewOrUndeleted() || area == null || ll == null || area.contains(ll.getX(), ll.getY())) {
            return true;
        }
        return false;
    }

    protected static boolean isInArea(Way way, Area area) {
        if (area == null) {
            return true;
        }

        for (Node n : way.getNodes()) {
            if (!isInArea(n, area)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Merge tags from source to target object, showing resolution dialog if
     * needed.
     *
     * @param source object tags are merged from
     * @param target object tags are merged to
     * @return The list of {@link Command commands} needed to apply resolution actions.
     * @throws UserCancelException If the user cancelled a dialog.
     */
    protected static List<Command> getTagConflictResolutionCommands(OsmPrimitive source, OsmPrimitive target) throws UserCancelException {
        Collection<OsmPrimitive> primitives = Arrays.asList(source, target);
        // launch a conflict resolution dialog, if necessary
        return CombinePrimitiveResolverDialog.launchIfNecessary(
                TagCollection.unionOfAllPrimitives(primitives), primitives, Collections.singleton(target));
    }

    /**
     * Find node from the collection which is nearest to <tt>node</tt>. Max distance is taken in consideration.
     * @return null if there is no such node.
     */
    protected static Node findNearestNode(Node node, Collection<Node> nodes) {
        if (nodes.contains(node))
            return node;

        Node nearest = null;
        // TODO: use meters instead of degrees, but do it fast
        double distance = Double.parseDouble(Config.getPref().get("utilsplugin2.replace-geometry.max-distance", "1"));
        LatLon coor = node.getCoor();

        for (Node n : nodes) {
            double d = n.getCoor().distance(coor);
            if (d < distance) {
                distance = d;
                nearest = n;
            }
        }
        return nearest;
    }

    /**
     * Return the nodes that appear only in 1st way , not in 2nd way.
     * @param way1 1st way
     * @param way2 2nd way
     * @return set of distinct nodes which appear only in first way
     */
    private static Set<Node> getDistinctNodes(Way way1, Way way2) {
        Set<Node> distincNodes = new HashSet<>(way1.getNodes());
        distincNodes.removeAll(way2.getNodes());
        return distincNodes;
    }
}
