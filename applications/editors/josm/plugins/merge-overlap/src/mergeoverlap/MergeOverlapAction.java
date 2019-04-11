package mergeoverlap;

import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.applyAutomaticTagConflictResolution;
import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.completeTagCollectionForEditing;
import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.normalizeTagCollectionBeforeEditing;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.corrector.ReverseWayTagCorrector;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.SplitWayCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.NodeGraph;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.UserCancelException;
import org.openstreetmap.josm.tools.Utils;

import mergeoverlap.hack.MyCombinePrimitiveResolverDialog;

/**
 * Merge overlapping part of ways.
 */
public class MergeOverlapAction extends JosmAction {

    Map<Way, List<Relation>> relations = new HashMap<>();
    Map<Way, Way> oldWays = new HashMap<>();
    Map<Relation, Relation> newRelations = new HashMap<>();
    Set<Way> deletes = new HashSet<>();

    /**
     * Constructs a new {@code MergeOverlapAction}.
     */
    public MergeOverlapAction() {
        super(tr("Merge overlap"), "merge_overlap",
                tr("Merge overlap of ways."), 
                Shortcut.registerShortcut("tools:mergeoverlap",tr("Tool: {0}", tr("Merge overlap")), KeyEvent.VK_O,
                Shortcut.ALT_CTRL), true);
    }

    /**
     * The action button has been clicked
     * 
     * @param e
     *            Action Event
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // List of selected ways
        List<Way> ways = new ArrayList<>();
        relations.clear();
        newRelations.clear();

        // For every selected way
        for (OsmPrimitive osm : getLayerManager().getEditDataSet().getSelected()) {
            if (osm instanceof Way && !osm.isDeleted()) {
                Way way = (Way) osm;
                ways.add(way);
                List<Relation> rels = new ArrayList<>();
                for (Relation r : Utils.filteredCollection(way.getReferrers(), Relation.class)) {
                    rels.add(r);
                }
                relations.put(way, rels);
            }
        }

        List<Way> sel = new ArrayList<>(ways);
        Collection<Command> cmds = new LinkedList<>();

        // *****
        // split
        // *****
        for (Way way : ways) {
            Set<Node> nodes = new HashSet<>();
            for (Way opositWay : ways) {
                if (way != opositWay) {
                    List<NodePos> nodesPos = new LinkedList<>();

                    int pos = 0;
                    for (Node node : way.getNodes()) {
                        int opositPos = 0;
                        for (Node opositNode : opositWay.getNodes()) {
                            if (node == opositNode) {
                                if (opositWay.isClosed()) {
                                    opositPos %= opositWay.getNodesCount() - 1;
                                }
                                nodesPos.add(new NodePos(node, pos, opositPos));
                                break;
                            }
                            opositPos++;
                        }
                        pos++;
                    }

                    NodePos start = null;
                    NodePos end = null;
                    int increment = 0;

                    boolean hasFirst = false;
                    for (NodePos node : nodesPos) {
                        if (start == null) {
                            start = node;
                        } else {
                            if (end == null) {
                                if (follows(way, opositWay, start, node, 1)) {
                                    end = node;
                                    increment = +1;
                                } else if (follows(way, opositWay, start, node, -1)) {
                                    end = node;
                                    increment = -1;
                                } else {
                                    start = node;
                                    end = null;
                                }
                            } else {
                                if (follows(way, opositWay, end, node, increment)) {
                                    end = node;
                                } else {
                                    hasFirst = addNodes(start, end, way, nodes, hasFirst);
                                    start = node;
                                    end = null;
                                }
                            }
                        }
                    }

                    if (start != null && end != null) {
                        hasFirst = addNodes(start, end, way, nodes, hasFirst);
                        start = null;
                        end = null;
                    }
                }
            }
            if (!nodes.isEmpty() && !way.isClosed() || nodes.size() >= 2) {
                List<List<Node>> wayChunks = SplitWayCommand.buildSplitChunks(way, new ArrayList<>(nodes));
                SplitWayCommand result = SplitWayCommand.splitWay(way, wayChunks, Collections.emptyList());

                cmds.add(result);
                sel.remove(way);
                sel.add(result.getOriginalWay());
                sel.addAll(result.getNewWays());
                List<Relation> rels = relations.remove(way);
                relations.put(result.getOriginalWay(), rels);
                for (Way w : result.getNewWays()) {
                    relations.put(w, rels);
                }
            }
        }

        // *****
        // merge
        // *****
        ways = new ArrayList<>(sel);
        while (!ways.isEmpty()) {
            Way way = ways.get(0);
            List<Way> combine = new ArrayList<>();
            combine.add(way);
            for (Way opositWay : ways) {
                if (way != opositWay && way.getNodesCount() == opositWay.getNodesCount()) {
                    boolean equals1 = true;
                    for (int i = 0; i < way.getNodesCount(); i++) {
                        if (way.getNode(i) != opositWay.getNode(i)) {
                            equals1 = false;
                            break;
                        }
                    }
                    boolean equals2 = true;
                    for (int i = 0; i < way.getNodesCount(); i++) {
                        if (way.getNode(i) != opositWay.getNode(way.getNodesCount() - i - 1)) {
                            equals2 = false;
                            break;
                        }
                    }
                    if (equals1 || equals2) {
                        combine.add(opositWay);
                    }
                }
            }
            ways.removeAll(combine);
            if (combine.size() > 1) {
                sel.removeAll(combine);
                // combine
                Pair<Way, List<Command>> combineResult;
                try {
                    combineResult = combineWaysWorker(combine);
                } catch (UserCancelException ex) {
                    return;
                }
                sel.add(combineResult.a);
                cmds.addAll(combineResult.b);
            }
        }

        for (Relation old : newRelations.keySet()) {
            cmds.add(new ChangeCommand(old, newRelations.get(old)));
        }

        List<Way> del = new LinkedList<>();
        for (Way w : deletes) {
            if (w.getDataSet() != null && !w.isDeleted()) {
                del.add(w);
            }
        }
        if (!del.isEmpty()) {
            cmds.add(new DeleteCommand(del));
        }

        // Commit
        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Merge Overlap (combine)"), cmds));
        getLayerManager().getEditDataSet().setSelected(sel);
        MainApplication.getMap().repaint();

        relations.clear();
        newRelations.clear();
        oldWays.clear();
    }

    private static class NodePos {
        Node node;
        int pos;
        int opositPos;

        NodePos(Node n, int p, int op) {
            node = n;
            pos = p;
            opositPos = op;
        }

        @Override
        public String toString() {
            return "NodePos: " + pos + ", " + opositPos + ", " + node;
        }
    }

    private boolean addNodes(NodePos start, NodePos end, Way way,
            Set<Node> nodes, boolean hasFirst) {
        if (way.isClosed() || (start.node != way.getNode(0) && start.node != way.getNode(way.getNodesCount() - 1))) {
            hasFirst = hasFirst || start.node == way.getNode(0);
            nodes.add(start.node);
        }
        if (way.isClosed() || (end.node != way.getNode(0) && end.node != way.getNode(way.getNodesCount() - 1))) {
            if (hasFirst && (end.node == way.getNode(way.getNodesCount() - 1))) {
                nodes.remove(way.getNode(0));
            } else {
                nodes.add(end.node);
            }
        }
        return hasFirst;
    }

    private boolean follows(Way way1, Way way2, NodePos np1, NodePos np2,
            int incr) {
        if (way2.isClosed() && incr == 1 && np1.opositPos == way2.getNodesCount() - 2) {
            return np2.pos == np1.pos + 1 && np2.opositPos == 0;
        } else if (way2.isClosed() && incr == 1 && np1.opositPos == 0) {
            return np2.pos == np1.pos && np2.opositPos == 0
                    || np2.pos == np1.pos + 1 && np2.opositPos == 1;
        } else if (way2.isClosed() && incr == -1 && np1.opositPos == 0) {
            return np2.pos == np1.pos && np2.opositPos == 0 || np2.pos == np1.pos + 1
                    && np2.opositPos == way2.getNodesCount() - 2;
        } else {
            return np2.pos == np1.pos + 1 && np2.opositPos == np1.opositPos + incr;
        }
    }

    /**
     * @param ways
     * @return null if ways cannot be combined. Otherwise returns the combined
     *         ways and the commands to combine
     * @throws UserCancelException
     */
    private Pair<Way, List<Command>> combineWaysWorker(Collection<Way> ways) throws UserCancelException {

        // prepare and clean the list of ways to combine
        if (ways == null || ways.isEmpty())
            return null;
        ways.remove(null); // just in case - remove all null ways from the collection

        // remove duplicates, preserving order
        ways = new LinkedHashSet<>(ways);

        // try to build a new way which includes all the combined ways
        NodeGraph graph = NodeGraph.createUndirectedGraphFromNodeWays(ways);
        List<Node> path = graph.buildSpanningPath();

        // check whether any ways have been reversed in the process
        // and build the collection of tags used by the ways to combine
        TagCollection wayTags = TagCollection.unionOfAllPrimitives(ways);

        List<Way> reversedWays = new LinkedList<>();
        List<Way> unreversedWays = new LinkedList<>();
        for (Way w : ways) {
            if ((path.indexOf(w.getNode(0)) + 1) == path.lastIndexOf(w.getNode(1))) {
                unreversedWays.add(w);
            } else {
                reversedWays.add(w);
            }
        }
        // reverse path if all ways have been reversed
        if (unreversedWays.isEmpty()) {
            Collections.reverse(path);
            unreversedWays = reversedWays;
            reversedWays = null;
        }
        if ((reversedWays != null) && !reversedWays.isEmpty()) {
            // filter out ways that have no direction-dependent tags
            unreversedWays = ReverseWayTagCorrector.irreversibleWays(unreversedWays);
            reversedWays = ReverseWayTagCorrector.irreversibleWays(reversedWays);
            // reverse path if there are more reversed than unreversed ways with
            // direction-dependent tags
            if (reversedWays.size() > unreversedWays.size()) {
                Collections.reverse(path);
                List<Way> tempWays = unreversedWays;
                unreversedWays = reversedWays;
                reversedWays = tempWays;
            }
            // if there are still reversed ways with direction-dependent tags,
            // reverse their tags
            if (!reversedWays.isEmpty()) {
                List<Way> unreversedTagWays = new ArrayList<>(ways);
                unreversedTagWays.removeAll(reversedWays);
                ReverseWayTagCorrector reverseWayTagCorrector = new ReverseWayTagCorrector();
                List<Way> reversedTagWays = new ArrayList<>();
                Collection<Command> changePropertyCommands = null;
                for (Way w : reversedWays) {
                    Way wnew = new Way(w);
                    reversedTagWays.add(wnew);
                    changePropertyCommands = reverseWayTagCorrector.execute(w, wnew);
                }
                if ((changePropertyCommands != null) && !changePropertyCommands.isEmpty()) {
                    for (Command c : changePropertyCommands) {
                        c.executeCommand();
                    }
                }
                wayTags = TagCollection.unionOfAllPrimitives(reversedTagWays);
                wayTags.add(TagCollection.unionOfAllPrimitives(unreversedTagWays));
            }
        }

        // create the new way and apply the new node list
        Way targetWay = getTargetWay(ways);
        Way modifiedTargetWay = new Way(targetWay);
        modifiedTargetWay.setNodes(path);

        TagCollection completeWayTags = new TagCollection(wayTags);
        applyAutomaticTagConflictResolution(completeWayTags);
        normalizeTagCollectionBeforeEditing(completeWayTags, ways);
        TagCollection tagsToEdit = new TagCollection(completeWayTags);
        completeTagCollectionForEditing(tagsToEdit);

        MyCombinePrimitiveResolverDialog dialog = MyCombinePrimitiveResolverDialog.getInstance();
        dialog.getTagConflictResolverModel().populate(tagsToEdit, completeWayTags.getKeysWithMultipleValues());
        dialog.setTargetPrimitive(targetWay);
        Set<Relation> parentRelations = getParentRelations(ways);
        dialog.getRelationMemberConflictResolverModel().populate(parentRelations, ways, oldWays);
        dialog.prepareDefaultDecisions();

        // resolve tag conflicts if necessary
        if (askForMergeTag(ways) || duplicateParentRelations(ways)) {
            dialog.setVisible(true);
            if (!dialog.isApplied())
                throw new UserCancelException();
        }

        List<Command> cmds = new LinkedList<>();
        deletes.addAll(ways);
        deletes.remove(targetWay);

        cmds.add(new ChangeCommand(getLayerManager().getEditDataSet(), targetWay, modifiedTargetWay));
        cmds.addAll(dialog.buildWayResolutionCommands());
        dialog.buildRelationCorrespondance(newRelations, oldWays);

        return new Pair<>(targetWay, cmds);
    }

    private static Way getTargetWay(Collection<Way> combinedWays) {
        // init with an arbitrary way
        Way targetWay = combinedWays.iterator().next();

        // look for the first way already existing on the server
        for (Way w : combinedWays) {
            targetWay = w;
            if (!w.isNew()) {
                break;
            }
        }
        return targetWay;
    }

    /**
     * @return has tag to be merged (=> ask)
     */
    private static boolean askForMergeTag(Collection<Way> ways) {
        for (Way way : ways) {
            for (Way oposite : ways) {
                for (String key : way.getKeys().keySet()) {
                    if (!"source".equals(key) && oposite.hasKey(key)
                            && !way.get(key).equals(oposite.get(key))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return has duplicate parent relation
     */
    private boolean duplicateParentRelations(Collection<Way> ways) {
        Set<Relation> relations = new HashSet<>();
        for (Way w : ways) {
            List<Relation> rs = getParentRelations(w);
            for (Relation r : rs) {
                if (relations.contains(r)) {
                    return true;
                }
            }
            relations.addAll(rs);
        }
        return false;
    }

    /**
     * Replies the set of referring relations
     * 
     * @return the set of referring relations
     */
    private List<Relation> getParentRelations(Way way) {
        List<Relation> rels = new ArrayList<>();
        for (Relation r : relations.get(way)) {
            if (newRelations.containsKey(r)) {
                rels.add(newRelations.get(r));
            } else {
                rels.add(r);
            }
        }
        return rels;
    }

    public static Relation getNew(Relation r, Map<Relation, Relation> newRelations) {
        if (newRelations.containsValue(r)) {
            return r;
        } else {
            Relation c = new Relation(r);
            newRelations.put(r, c);
            return c;
        }
    }
/*
    private Way getOld(Way r) {
        return getOld(r, oldWays);
    }*/

    public static Way getOld(Way w, Map<Way, Way> oldWays) {
        if (oldWays.containsKey(w)) {
            return oldWays.get(w);
        } else {
            return w;
        }
    }

    /**
     * Replies the set of referring relations
     * 
     * @return the set of referring relations
     */
    private Set<Relation> getParentRelations(Collection<Way> ways) {
        Set<Relation> ret = new HashSet<>();
        for (Way w : ways) {
            ret.addAll(getParentRelations(w));
        }
        return ret;
    }

    /** Enable this action only if something is selected */
    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
        }
    }

    /** Enable this action only if something is selected */
    @Override
    protected void updateEnabledState(
            Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        for (OsmPrimitive primitive : selection) {
            if (!(primitive instanceof Way) || primitive.isDeleted()) {
                setEnabled(false);
                return;
            }
        }
        setEnabled(selection.size() >= 2);
    }
}
