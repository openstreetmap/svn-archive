package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    private final List<Command> cmds = new LinkedList<Command>();
    private final List<OsmPrimitive> undeletedPrimitives = new LinkedList<OsmPrimitive>();

    /**
     * constructor
     */
    public DataSetCommandMerger(DataSet sourceDataSet, DataSet targetDataSet) {
        this.sourceDataSet = sourceDataSet;
        this.targetDataSet = targetDataSet;
        merge();
    }
    
    private void addChangeCommandIfNotEquals(OsmPrimitive target, OsmPrimitive newTarget) {
        if (!target.hasEqualSemanticAttributes(newTarget)) {
            cmds.add(new ChangeCommand(target,newTarget));
            undeletedPrimitives.add(target);
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
        addChangeCommandIfNotEquals(target,newTarget);
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

        List<Node> newNodes = new ArrayList<Node>(source.getNodesCount());
        for (Node sourceNode : source.getNodes()) {
            Node targetNode = (Node)getMergeTarget(sourceNode);
            if (!targetNode.isDeleted() || undeletedPrimitives.contains(targetNode)) {
                newNodes.add(targetNode);
            } else if (sourceNode.isIncomplete()
                    && !conflicts.hasConflictForMy(targetNode)) {
                conflicts.add(new Conflict<OsmPrimitive>(targetNode, sourceNode, true));
                Node undeletedTargetNode = new Node(targetNode);
                undeletedTargetNode.setDeleted(false);
                addChangeCommandIfNotEquals(targetNode,undeletedTargetNode);
            }
        }
        Way newTarget = new Way(target);
        mergePrimitive(source, target, newTarget);
        newTarget.setNodes(newNodes);
        addChangeCommandIfNotEquals(target,newTarget);
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
        LinkedList<RelationMember> newMembers = new LinkedList<RelationMember>();
        for (RelationMember sourceMember : source.getMembers()) {
            OsmPrimitive targetMember = getMergeTarget(sourceMember.getMember());
            if (targetMember.isDeleted() && sourceMember.getMember().isIncomplete()
                    && !conflicts.hasConflictForMy(targetMember)) {
                conflicts.add(new Conflict<OsmPrimitive>(targetMember, sourceMember.getMember(), true));
                OsmPrimitive undeletedTargetMember;
                switch(targetMember.getType()) {
                case NODE: undeletedTargetMember = new Node((Node)targetMember); break;
                case WAY: undeletedTargetMember = new Way((Way)targetMember); break;
                case RELATION: undeletedTargetMember = new Relation((Relation)targetMember); break;
                default: throw new AssertionError();
                }
                undeletedTargetMember.setDeleted(false);
                addChangeCommandIfNotEquals(targetMember,undeletedTargetMember);
            }
            newMembers.add(new RelationMember(sourceMember.getRole(), targetMember));
        }
        Relation newRelation = new Relation(target);
        mergePrimitive(source, target, newRelation);
        newRelation.setMembers(newMembers);
        addChangeCommandIfNotEquals(target,newRelation);
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
