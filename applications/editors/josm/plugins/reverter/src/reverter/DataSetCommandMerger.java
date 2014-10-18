package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.conflict.Conflict;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Modified {@see org.openstreetmap.josm.data.osm.DataSetMerger} that
 * produces list of commands instead of directly merging layers.
 *
 */
final class DataSetCommandMerger {

    /** the collection of conflicts created during merging */
    private final ConflictCollection conflicts = new ConflictCollection();
    /** the source dataset where primitives are merged from */
    private final DataSet sourceDataSet;
    private final DataSet targetDataSet;

    private final List<Command> cmds = new LinkedList<>();
    private final List<OsmPrimitive> nominalRevertedPrimitives = new LinkedList<>();

    /**
     * constructor
     */
    public DataSetCommandMerger(DataSet sourceDataSet, DataSet targetDataSet) {
        this.sourceDataSet = sourceDataSet;
        this.targetDataSet = targetDataSet;
        merge();
    }
    
    private void addChangeCommandIfNotEquals(OsmPrimitive target, OsmPrimitive newTarget, boolean nominal) {
        if (!target.hasEqualSemanticAttributes(newTarget)) {
            cmds.add(new ChangeCommand(target,newTarget));
            if (nominal) {
                nominalRevertedPrimitives.add(target);
            }
            Main.debug("Reverting "+target+" to "+newTarget);
        }
    }

    private OsmPrimitive getMergeTarget(OsmPrimitive mergeSource) {
        OsmPrimitive p = targetDataSet.getPrimitiveById(mergeSource.getId(), mergeSource.getType());
        if (p == null)
            throw new IllegalStateException(tr("Missing merge target of type {0} with id {1}",
                    mergeSource.getType(), mergeSource.getUniqueId()));
        return p;
    }

    private void mergePrimitive(OsmPrimitive source, OsmPrimitive target, OsmPrimitive newTarget) {
        newTarget.mergeFrom(source);
        newTarget.setOsmId(target.getId(), (int)target.getVersion());
        newTarget.setVisible(target.isVisible());
        newTarget.setDeleted(false);
    }

    /**
     * Merges the source node onto its target node.
     *
     * @param source the source way
     */
    private void mergeNode(Node source) {
        if (source.isIncomplete()) return;
        if (!source.isVisible()) return;
        Node target = (Node)getMergeTarget(source);

        Node newTarget = new Node(target);
        mergePrimitive(source, target, newTarget);
        addChangeCommandIfNotEquals(target,newTarget,true);
    }

    /**
     * Merges the source way onto its target way.
     *
     * @param source the source way
     * @throws IllegalStateException thrown if no target way can be found for the source way
     * @throws IllegalStateException thrown if there isn't a target node for one of the nodes in the source way
     *
     */
    private void mergeWay(Way source) throws IllegalStateException {
        if (source.isIncomplete()) return;
        if (!source.isVisible()) return;
        Way target = (Way)getMergeTarget(source);
        
        Collection<Conflict<OsmPrimitive>> localConflicts = new ArrayList<>();

        List<Node> newNodes = new ArrayList<>(source.getNodesCount());
        for (Node sourceNode : source.getNodes()) {
            Node targetNode = (Node)getMergeTarget(sourceNode);
            // Target node is not deleted or it will be undeleted when running existing commands
            if (!targetNode.isDeleted() || nominalRevertedPrimitives.contains(targetNode)) {
                newNodes.add(targetNode);
            // Target node has been deleted by a more recent changeset -> conflict
            } else if (sourceNode.isIncomplete() && !conflicts.hasConflictForMy(targetNode)) {
                localConflicts.add(new Conflict<OsmPrimitive>(targetNode, sourceNode, true));
            } else {
                Main.info("Skipping target node "+targetNode+" for source node "+sourceNode+" while reverting way "+source);
            }
        }
        Way newTarget = new Way(target);
        mergePrimitive(source, target, newTarget);
        newTarget.setNodes(newNodes);
        if (newNodes.isEmpty()) {
            Main.error("Unable to revert "+source+" as it produces 0 nodes way "+newTarget);
        } else {
            for (Conflict<OsmPrimitive> c : localConflicts) {
                Main.warn("New conflict: "+c);
                conflicts.add(c);
                Node targetNode = (Node)c.getTheir();
                Node undeletedTargetNode = new Node(targetNode);
                undeletedTargetNode.setDeleted(false);
                addChangeCommandIfNotEquals(targetNode,undeletedTargetNode,false);
            }
            addChangeCommandIfNotEquals(target,newTarget,true);
        }
    }

    /**
     * Merges the source relation onto the corresponding target relation.
     * @param source the source relation
     * @throws IllegalStateException thrown if there is no corresponding target relation
     * @throws IllegalStateException thrown if there isn't a corresponding target object for one of the relation
     * members in source
     */
    private void mergeRelation(Relation source) throws IllegalStateException {
        if (source.isIncomplete()) return;
        if (!source.isVisible()) return;
        Relation target = (Relation) getMergeTarget(source);
        LinkedList<RelationMember> newMembers = new LinkedList<>();
        for (RelationMember sourceMember : source.getMembers()) {
            OsmPrimitive targetMember = getMergeTarget(sourceMember.getMember());
            if (targetMember.isDeleted() && sourceMember.getMember().isIncomplete()
                    && !conflicts.hasConflictForMy(targetMember)) {
                conflicts.add(new Conflict<>(targetMember, sourceMember.getMember(), true));
                OsmPrimitive undeletedTargetMember;
                switch(targetMember.getType()) {
                case NODE: undeletedTargetMember = new Node((Node)targetMember); break;
                case WAY: undeletedTargetMember = new Way((Way)targetMember); break;
                case RELATION: undeletedTargetMember = new Relation((Relation)targetMember); break;
                default: throw new AssertionError();
                }
                undeletedTargetMember.setDeleted(false);
                addChangeCommandIfNotEquals(targetMember,undeletedTargetMember,false);
            }
            newMembers.add(new RelationMember(sourceMember.getRole(), targetMember));
        }
        Relation newRelation = new Relation(target);
        mergePrimitive(source, target, newRelation);
        newRelation.setMembers(newMembers);
        addChangeCommandIfNotEquals(target,newRelation,true);
    }
    
    private void merge() {
        for (Node node: sourceDataSet.getNodes()) {
            mergeNode(node);
        }
        for (Way way: sourceDataSet.getWays()) {
            mergeWay(way);
        }
        for (Relation relation: sourceDataSet.getRelations()) {
            mergeRelation(relation);
        }
    }

    public List<Command> getCommandList() {
        return cmds;
    }

    /**
     * replies the map of conflicts
     *
     * @return the map of conflicts
     */
    public ConflictCollection getConflicts() {
        return conflicts;
    }
}
