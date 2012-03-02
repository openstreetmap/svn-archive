package utilsplugin2.dumbutils;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.RelationToChildReference;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Replaces already existing object (id>0) with a new object (id<0).
 *
 * @author Zverik
 */
public class ReplaceGeometryAction extends JosmAction {
    private static final String TITLE = tr("Replace Geometry");

    public ReplaceGeometryAction() {
        super(TITLE, "dumbutils/replacegeometry", tr("Replace geometry of selected object with a new one"),
                Shortcut.registerShortcut("tools:replacegeometry", tr("Tool: {0}", tr("Replace Geometry")), KeyEvent.VK_G, Shortcut.CTRL_SHIFT)
                , true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (getCurrentDataSet() == null) {
            return;
        }

        // There must be two ways selected: one with id > 0 and one new.
        List<OsmPrimitive> selection = new ArrayList<OsmPrimitive>(getCurrentDataSet().getSelected());
        if (selection.size() != 2) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("This tool replaces geometry of one object with another, and so requires exactly two objects to be selected."),
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        OsmPrimitive firstObject = selection.get(0);
        OsmPrimitive secondObject = selection.get(1);
        
        try {
            replace(firstObject, secondObject);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(Main.parent,
                    ex.getMessage(), TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
         
    }
    public boolean replace(OsmPrimitive firstObject, OsmPrimitive secondObject) {
        if (firstObject instanceof Way && secondObject instanceof Way) {
            return replaceWayWithWay(Arrays.asList((Way) firstObject, (Way) secondObject));
        } else if (firstObject instanceof Node && secondObject instanceof Node) {
            throw new IllegalArgumentException(tr("To replace a node with a node, use the node merge tool."));
        } else if (firstObject instanceof Node) {
            return replaceNode((Node) firstObject, secondObject);
        } else if (secondObject instanceof Node) {
            return replaceNode((Node) secondObject, firstObject);
        } else {
            throw new IllegalArgumentException(tr("This tool can only replace a node with a way, a node with a multipolygon, or a way with a way."));
        }
    }

    /**
     * Replace or upgrade a node to a way or multipolygon
     *
     * @param subjectNode node to be replaced
     * @param referenceObject object with greater spatial quality
     */
    public boolean replaceNode(Node subjectNode, OsmPrimitive referenceObject) {
        if (!OsmPrimitive.getFilteredList(subjectNode.getReferrers(), Way.class).isEmpty()) {
            JOptionPane.showMessageDialog(Main.parent, tr("Node belongs to way(s), cannot replace."),
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        if (referenceObject instanceof Relation && !((Relation) referenceObject).isMultipolygon()) {
            JOptionPane.showMessageDialog(Main.parent, tr("Relation is not a multipolygon, cannot be used as a replacement."),
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        Node nodeToReplace = null;
        // see if we need to replace a node in the replacement way to preserve connection in history
        if (!subjectNode.isNew()) {
            // Prepare a list of nodes that are not important
            Collection<Node> nodePool = new HashSet<Node>();
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

        List<Command> commands = new ArrayList<Command>();
        AbstractMap<String, String> nodeTags = (AbstractMap<String, String>) subjectNode.getKeys();

        // merge tags
        Collection<Command> tagResolutionCommands = getTagConflictResolutionCommands(subjectNode, referenceObject);
        if (tagResolutionCommands == null) {
            // user canceled tag merge dialog
            return false;
        }
        commands.addAll(tagResolutionCommands);

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

        getCurrentDataSet().setSelected(referenceObject);

        Main.main.undoRedo.add(new SequenceCommand(
                tr("Replace geometry for node {0}", subjectNode.getDisplayName(DefaultNameFormatter.getInstance())),
                commands));
        return true;
    }
    
    public boolean replaceWayWithWay(List<Way> selection) {
        // determine which way will be replaced and which will provide the geometry
        boolean overrideNewCheck = false;
        int idxNew = selection.get(0).isNew() ? 0 : 1;
        if( selection.get(1-idxNew).isNew() ) {
            // if both are new, select the one with all the DB nodes
            boolean areNewNodes = false;
            for (Node n : selection.get(0).getNodes()) {
                if (n.isNew()) {
                    areNewNodes = true;
                }
            }
            idxNew = areNewNodes ? 0 : 1;
            overrideNewCheck = true;
            for (Node n : selection.get(1 - idxNew).getNodes()) {
                if (n.isNew()) {
                    overrideNewCheck = false;
                }
            }
        }
        Way referenceWay = selection.get(idxNew);
        Way subjectWay = selection.get(1 - idxNew);
        
        if( !overrideNewCheck && (subjectWay.isNew() || !referenceWay.isNew()) ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please select one way that exists in the database and one new way with correct geometry."),
                    TITLE, JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return replaceWayWithWay(subjectWay, referenceWay);
    }
    
    public static boolean replaceWayWithWay(Way subjectWay, Way referenceWay) {

        Area a = getCurrentDataSet().getDataSourceArea();
        if (!isInArea(subjectWay, a) || !isInArea(referenceWay, a)) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("The ways must be entirely within the downloaded area."),
                    TITLE, JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (hasImportantNode(referenceWay, subjectWay)) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("The way to be replaced cannot have any nodes with properties or relation memberships unless they belong to both ways."),
                    TITLE, JOptionPane.WARNING_MESSAGE);
            return false;
        }

        List<Command> commands = new ArrayList<Command>();
                
        // merge tags
        Collection<Command> tagResolutionCommands = getTagConflictResolutionCommands(referenceWay, subjectWay);
        if (tagResolutionCommands == null) {
            // user canceled tag merge dialog
            return false;
        }
        commands.addAll(tagResolutionCommands);
        
        // Prepare a list of nodes that are not used anywhere except in the way
        Collection<Node> nodePool = getUnimportantNodes(subjectWay);

        // And the same for geometry, list nodes that can be freely deleted
        Set<Node> geometryPool = new HashSet<Node>();
        for( Node node : referenceWay.getNodes() ) {
            List<OsmPrimitive> referrers = node.getReferrers();
            if( node.isNew() && !node.isDeleted() && referrers.size() == 1
                    && referrers.get(0).equals(referenceWay) && !subjectWay.containsNode(node)
                    && !hasInterestingKey(node))
                geometryPool.add(node);
        }

        // Find new nodes that are closest to the old ones, remove matching old ones from the pool
        Map<Node, Node> nodeAssoc = new HashMap<Node, Node>();
        for( Node n : geometryPool ) {
            Node nearest = findNearestNode(n, nodePool);
            if( nearest != null ) {
                nodeAssoc.put(n, nearest);
                nodePool.remove(nearest);
            }
        }

        // Now that we have replacement list, move all unused new nodes to nodePool (and delete them afterwards)
        for( Node n : geometryPool )
            if( nodeAssoc.containsKey(n) )
                nodePool.add(n);

        // And prepare a list of nodes with all the replacements
        List<Node> geometryNodes = referenceWay.getNodes();
        for( int i = 0; i < geometryNodes.size(); i++ )
            if( nodeAssoc.containsKey(geometryNodes.get(i)) )
                geometryNodes.set(i, nodeAssoc.get(geometryNodes.get(i)));

        // Now do the replacement
        commands.add(new ChangeNodesCommand(subjectWay, geometryNodes));

        // Move old nodes to new positions
        for( Node node : nodeAssoc.keySet() )
            commands.add(new MoveCommand(nodeAssoc.get(node), node.getCoor()));

        // Remove geometry way from selection
        getCurrentDataSet().clearSelection(referenceWay);

        // And delete old geometry way
        commands.add(new DeleteCommand(referenceWay));

        // Delete nodes that are not used anymore
        if( !nodePool.isEmpty() )
            commands.add(new DeleteCommand(nodePool));

        // Two items in undo stack: change original way and delete geometry way
        Main.main.undoRedo.add(new SequenceCommand(
                tr("Replace geometry for way {0}", subjectWay.getDisplayName(DefaultNameFormatter.getInstance())),
                commands));
        return true;
    }

    /**
     * Create a list of nodes that are not used anywhere except in the way.
     *
     * @param way
     * @return 
     */
    protected static Collection<Node> getUnimportantNodes(Way way) {
        Set<Node> nodePool = new HashSet<Node>();
        for (Node n : way.getNodes()) {
            List<OsmPrimitive> referrers = n.getReferrers();
            if (!n.isDeleted() && referrers.size() == 1 && referrers.get(0).equals(way)
                    && !hasInterestingKey(n)) {
                nodePool.add(n);
            }
        }
        return nodePool;
    }
    
    /**
     * Checks if a way has at least one important node (e.g. interesting tag,
     * role membership), and thus cannot be safely modified.
     * 
     * @param way
     * @return 
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
        if (node.isNewOrUndeleted() || area == null || area.contains(node.getCoor())) {
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
     * @return
     */
    protected static List<Command> getTagConflictResolutionCommands(OsmPrimitive source, OsmPrimitive target) {
        Collection<OsmPrimitive> primitives = Arrays.asList(source, target);
        
        Set<RelationToChildReference> relationToNodeReferences = RelationToChildReference.getRelationToChildReferences(primitives);

        // build the tag collection
        TagCollection tags = TagCollection.unionOfAllPrimitives(primitives);
        TagConflictResolutionUtil.combineTigerTags(tags);
        TagConflictResolutionUtil.normalizeTagCollectionBeforeEditing(tags, primitives);
        TagCollection tagsToEdit = new TagCollection(tags);
        TagConflictResolutionUtil.completeTagCollectionForEditing(tagsToEdit);

        // launch a conflict resolution dialog, if necessary
        CombinePrimitiveResolverDialog dialog = CombinePrimitiveResolverDialog.getInstance();
        dialog.getTagConflictResolverModel().populate(tagsToEdit, tags.getKeysWithMultipleValues());
        dialog.getRelationMemberConflictResolverModel().populate(relationToNodeReferences);
        dialog.setTargetPrimitive(target);
        dialog.prepareDefaultDecisions();

        // conflict resolution is necessary if there are conflicts in the merged tags
        // or if at least one of the merged nodes is referred to by a relation
        if (!tags.isApplicableToPrimitive() || relationToNodeReferences.size() > 1) {
            dialog.setVisible(true);
            if (dialog.isCanceled()) {
                return null;
            }
        }
        return dialog.buildResolutionCommands();
    }

    
    /**
     * Find node from the collection which is nearest to <tt>node</tt>. Max distance is taken in consideration.
     * @return null if there is no such node.
     */
    protected static Node findNearestNode( Node node, Collection<Node> nodes ) {
        if( nodes.contains(node) )
            return node;
        
        Node nearest = null;
        // TODO: use meters instead of degrees, but do it fast
        double distance = Double.parseDouble(Main.pref.get("utilsplugin2.replace-geometry.max-distance", "1"));
        Point2D coor = node.getCoor();

        for( Node n : nodes ) {
            double d = n.getCoor().distance(coor);
            if( d < distance ) {
                distance = d;
                nearest = n;
            }
        }
        return nearest;
    }

    @Override
    protected void updateEnabledState() {
        if( getCurrentDataSet() == null ) {
            setEnabled(false);
        }  else
            updateEnabledState(getCurrentDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
        setEnabled(selection != null && selection.size() >= 2 );
    }
}

