package ext_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;

final class DataSetToCmd {

    /** the source dataset where primitives are merged from */
    private final DataSet sourceDataSet;

    /**
     * A map of all primitives that got replaced with other primitives. Key is
     * the PrimitiveId in their dataset, the value is the PrimitiveId in my
     * dataset
     */
    private final Map<PrimitiveId, OsmPrimitive> mergedMap = new HashMap<PrimitiveId, OsmPrimitive>();

    private final LinkedList<Command> cmds = new LinkedList<Command>();

    /**
     * constructor
     */
    public DataSetToCmd(DataSet sourceDataSet) {
        this.sourceDataSet = sourceDataSet;
        merge();
    }

    /**
     * Merges a primitive <code>other</code> of type
     * <P>
     * onto my primitives.
     *
     * @param <P>
     *            the type of the other primitive
     * @param source
     *            the other primitive
     */
    private void mergePrimitive(OsmPrimitive source) {

        OsmPrimitive target = null;
        switch (source.getType()) {
        case NODE:
            target = new Node();
            break;
        case WAY:
            target = new Way();
            break;
        case RELATION:
            target = new Relation();
            break;
        default:
            throw new AssertionError();
        }
        target.mergeFrom(source);
        mergedMap.put(source.getPrimitiveId(), target);
        cmds.add(new AddCommand(target));
    }

    private OsmPrimitive getMergeTarget(OsmPrimitive mergeSource)
            throws IllegalStateException {
        OsmPrimitive target = mergedMap.get(mergeSource.getPrimitiveId());
        if (target == null)
            return null;
        return target;
    }

    /**
     * Postprocess the dataset and fix all merged references to point to the
     * actual data.
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
     * @param source
     *            the source way
     * @throws IllegalStateException
     *             thrown if no target way can be found for the source way
     * @throws IllegalStateException
     *             thrown if there isn't a target node for one of the nodes in
     *             the source way
     *
     */
    private void mergeNodeList(Way source) throws IllegalStateException {
        Way target = (Way) getMergeTarget(source);
        if (target == null)
            throw new IllegalStateException(tr(
                    "Missing merge target for way with id {0}", source.getUniqueId()));
        if (target.getDataSet() != null)
            throw new IllegalStateException(tr(
                    "Missing merge target for way with id {0}", source.getUniqueId()));

        List<Node> newNodes = new ArrayList<Node>(source.getNodesCount());
        for (Node sourceNode : source.getNodes()) {
            Node targetNode = (Node) getMergeTarget(sourceNode);
            if (targetNode == null)
                throw new IllegalStateException(tr(
                        "Missing merge target for node with id {0}", sourceNode
                                .getUniqueId()));
            if (targetNode.getDataSet() != null)
                throw new IllegalStateException(tr(
                        "Missing merge target for way with id {0}", source.getUniqueId()));

            newNodes.add(targetNode);
        }
        cmds.add(new ChangeNodesCommand(target, newNodes));
    }

    /**
     * Merges the relation members of a source relation onto the corresponding
     * target relation.
     *
     * @param source
     *            the source relation
     * @throws IllegalStateException
     *             thrown if there is no corresponding target relation
     * @throws IllegalStateException
     *             thrown if there isn't a corresponding target object for one
     *             of the relation members in source
     */
    private void mergeRelationMembers(Relation source) throws IllegalStateException {
        Relation target = (Relation) getMergeTarget(source);
        if (target == null)
            throw new IllegalStateException(
                    tr("Missing merge target for relation with id {0}", source
                            .getUniqueId()));
        if (target.getDataSet() != null)
            throw new IllegalStateException(
                    tr("Missing merge target for relation with id {0}", source
                            .getUniqueId()));
        LinkedList<RelationMember> newMembers = new LinkedList<RelationMember>();
        for (RelationMember sourceMember : source.getMembers()) {
            OsmPrimitive targetMember = getMergeTarget(sourceMember.getMember());
            if (targetMember == null)
                throw new IllegalStateException(tr(
                        "Missing merge target of type {0} with id {1}", sourceMember
                                .getType(), sourceMember.getUniqueId()));
            if (targetMember.getDataSet() != null)
                throw new IllegalStateException(tr(
                        "Missing merge target of type {0} with id {1}", sourceMember
                                .getType(), sourceMember.getUniqueId()));
            newMembers.add(new RelationMember(sourceMember.getRole(), targetMember));
        }
        Relation newRelation = new Relation(target);
        newRelation.setMembers(newMembers);
        cmds.add(new ChangeCommand(target, newRelation));
    }

    private void merge() {
        for (Node node : sourceDataSet.getNodes()) {
            mergePrimitive(node);
        }
        for (Way way : sourceDataSet.getWays()) {
            mergePrimitive(way);
        }
        for (Relation relation : sourceDataSet.getRelations()) {
            mergePrimitive(relation);
        }
        fixReferences();
    }

    public LinkedList<Command> getCommandList() {
        return cmds;
    }
}
