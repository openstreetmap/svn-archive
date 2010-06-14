package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.conflict.Conflict;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.data.osm.*;

/**
 * Modified {@see org.openstreetmap.josm.data.osm.DataSetMerger} that
 * produces list of commands instead of directly merging layers.
 *
 */
final class DataSetToCmd {

    /** the collection of conflicts created during merging */
    private final ConflictCollection conflicts = new ConflictCollection();
    /** the source dataset where primitives are merged from */
    private final DataSet sourceDataSet;
    private final DataSet targetDataSet;

    private final LinkedList<Command> cmds = new LinkedList<Command>();

    /**
     * constructor
     */
    public DataSetToCmd(DataSet sourceDataSet, DataSet targetDataSet) {
        this.sourceDataSet = sourceDataSet;
        this.targetDataSet = targetDataSet;
        merge();
    }

    /**
     * Merges a primitive <code>other</code> of type <P> onto my primitives.
     *
     * @param <P>  the type of the other primitive
     * @param source  the other primitive
     */
    private void mergePrimitive(OsmPrimitive source) {
        if (source.isIncomplete()) return;
        if (!source.isVisible()) return;
        OsmPrimitive target = getMergeTarget(source);
        if (target.getVersion() == 0)
            throw new IllegalStateException(tr("Target of type {0} with id {1} has invalid version",
                    target.getType(), target.getUniqueId()));
        OsmPrimitive newTarget;
        switch(target.getType()) {
        case NODE: newTarget = new Node((Node)target); break;
        case WAY: newTarget = new Way((Way)target); break;
        case RELATION: newTarget = new Relation((Relation)target); break;
        default: throw new AssertionError();
        }
        newTarget.mergeFrom(source);
        newTarget.setOsmId(target.getId(), (int)target.getVersion());
        newTarget.setDeleted(false);
        cmds.add(new ChangeCommand(target,newTarget));
    }

    private OsmPrimitive getMergeTarget(OsmPrimitive mergeSource) {
        OsmPrimitive p = targetDataSet.getPrimitiveById(mergeSource.getId(), mergeSource.getType());
        if (p == null)
            throw new IllegalStateException(tr("Missing merge target of type {0} with id {1}",
                    mergeSource.getType(), mergeSource.getUniqueId()));
        return p;
    }
    
    /**
     * Postprocess the dataset and fix all merged references to point to the actual
     * data.
     */
    public void fixReferences() {
        for (Way w : sourceDataSet.getWays()) {
                mergeNodeList(w);
        }
        for (Relation r : sourceDataSet.getRelations()) {
                mergeRelationMembers(r);
        }
    }

    /**
     * Merges the node list of a source way onto its target way.
     *
     * @param source the source way
     * @throws IllegalStateException thrown if no target way can be found for the source way
     * @throws IllegalStateException thrown if there isn't a target node for one of the nodes in the source way
     *
     */
    private void mergeNodeList(Way source) throws IllegalStateException {
        if (source.isIncomplete()) return;
        if (!source.isVisible()) return;
        Way target = (Way)getMergeTarget(source);

        List<Node> newNodes = new ArrayList<Node>(source.getNodesCount());
        for (Node sourceNode : source.getNodes()) {
            Node targetNode = (Node)getMergeTarget(sourceNode);
            if (targetNode.isDeleted() && sourceNode.isIncomplete()
                    && !conflicts.hasConflictForMy(targetNode)) {
                conflicts.add(new Conflict<OsmPrimitive>(targetNode, sourceNode, true));
                Node undeletedTargetNode = new Node(targetNode);
                undeletedTargetNode.setDeleted(false);
                cmds.add(new ChangeCommand(targetNode,undeletedTargetNode));
            }
            newNodes.add(targetNode);
        }
        cmds.add(new ChangeNodesCommand(target,newNodes));
    }

    /**
     * Merges the relation members of a source relation onto the corresponding target relation.
     * @param source the source relation
     * @throws IllegalStateException thrown if there is no corresponding target relation
     * @throws IllegalStateException thrown if there isn't a corresponding target object for one of the relation
     * members in source
     */
    private void mergeRelationMembers(Relation source) throws IllegalStateException {
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
                cmds.add(new ChangeCommand(targetMember,undeletedTargetMember));
            }
            newMembers.add(new RelationMember(sourceMember.getRole(), targetMember));
        }
        Relation newRelation = new Relation(target); 
        newRelation.setMembers(newMembers);
        cmds.add(new ChangeCommand(target,newRelation));
    }
    private void merge() {
        for (Node node: sourceDataSet.getNodes()) {
            mergePrimitive(node);
        }
        for (Way way: sourceDataSet.getWays()) {
            mergePrimitive(way);
        }
        for (Relation relation: sourceDataSet.getRelations()) {
            mergePrimitive(relation);
        }
        fixReferences();
    }

    public LinkedList<Command> getCommandList() {
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
