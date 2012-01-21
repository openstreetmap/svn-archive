package utilsplugin2.dumbutils;

import java.awt.geom.Point2D;
import java.awt.geom.Area;
import org.openstreetmap.josm.data.osm.Node;
import java.util.*;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.Main;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.data.osm.Way;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.tools.Shortcut;
import java.awt.event.ActionEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Replaces already existing object (id>0) with a new object (id<0).
 *
 * @author Zverik
 */
public class ReplaceGeometryAction extends JosmAction {
    private static final String TITLE = tr("Replace Geometry");
    private static final double MAX_NODE_REPLACEMENT_DISTANCE = 3e-4;

    public ReplaceGeometryAction() {
        super(TITLE, "dumbutils/replacegeometry", tr("Replace geometry of selected object with a new one"),
                Shortcut.registerShortcut("tools:replacegeometry", tr("Tool: {0}", TITLE), KeyEvent.VK_G,
                Shortcut.GROUP_HOTKEY, Shortcut.SHIFT_DEFAULT), true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (getCurrentDataSet() == null) {
            return;
        }

        // There must be two ways selected: one with id > 0 and one new.
        List<OsmPrimitive> selection = new ArrayList(getCurrentDataSet().getSelected());
        if (selection.size() != 2) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("This tool replaces geometry of one object with another, and so requires exactly two objects to be selected."),
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        OsmPrimitive firstObject = selection.get(0);
        OsmPrimitive secondObject = selection.get(1);

        if (firstObject instanceof Way && secondObject instanceof Way) {
            replaceWayWithWay(Arrays.asList((Way) firstObject, (Way) secondObject));
        } else if (firstObject instanceof Node && secondObject instanceof Way) {
            replaceNodeWithWay((Node) firstObject, (Way) secondObject);
        } else if (secondObject instanceof Node && firstObject instanceof Way) {
            replaceNodeWithWay((Node) secondObject, (Way) firstObject);
        } else {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("This tool can only replace a node with a way, or a way with a way."),
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }
    
    public void replaceNodeWithWay(Node node, Way way) {
        if (!node.getReferrers().isEmpty()) {
            JOptionPane.showMessageDialog(Main.parent, tr("Node has referrers, cannot replace with way."), TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Node nodeToReplace = null;
        // see if we need to replace a node in the replacement way to preserve connection in history
        if (!node.isNew()) {
            // Prepare a list of nodes that are not used anywhere except in the way
            Collection<Node> nodePool = getUnimportantNodes(way);
            nodeToReplace = findNearestNode(node, nodePool);

            if (nodeToReplace == null && !nodePool.isEmpty()) {
                // findNearestNode failed, just pick the first unimportant node
                nodeToReplace = nodePool.iterator().next();
            }
        }

        List<Command> commands = new ArrayList<Command>();
        AbstractMap<String, String> nodeTags = (AbstractMap<String, String>) node.getKeys();

        // replace sacrificial node in way with node that is being upgraded
        if (nodeToReplace != null) {
            List<Node> wayNodes = way.getNodes();
            int idx = wayNodes.indexOf(nodeToReplace);
            wayNodes.set(idx, node);
            if (idx == 0 && way.isClosed()) {
                // node is at start/end of way
                wayNodes.set(wayNodes.size() - 1, node);
            }
            commands.add(new ChangeNodesCommand(way, wayNodes));
            commands.add(new MoveCommand(node, nodeToReplace.getCoor()));
            commands.add(new DeleteCommand(nodeToReplace));

            // delete tags from node
            if (!nodeTags.isEmpty()) {
                for (String key : nodeTags.keySet()) {
                    commands.add(new ChangePropertyCommand(node, key, null));
                }

            }
        } else {
            // no node to replace, so just delete the original node
            commands.add(new DeleteCommand(node));
        }

        // Copy tags from node
        // TODO: use merge tag conflict dialog instead
        for (String key : nodeTags.keySet()) {
            commands.add(new ChangePropertyCommand(way, key, nodeTags.get(key)));
        }

        getCurrentDataSet().setSelected(way);

        Main.main.undoRedo.add(new SequenceCommand(
                tr("Replace geometry for way {0}", way.getDisplayName(DefaultNameFormatter.getInstance())),
                commands));
    }
    
    public void replaceWayWithWay(List<Way> selection) {
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
        Way geometry = selection.get(idxNew);
        Way way = selection.get(1 - idxNew);
        if( !overrideNewCheck && (way.isNew() || !geometry.isNew()) ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please select one way that exists in the database and one new way with correct geometry."),
                    TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prepare a list of nodes that are not used anywhere except in the way
        Collection<Node> nodePool = getUnimportantNodes(way);

        // And the same for geometry, list nodes that can be freely deleted
        Set<Node> geometryPool = new HashSet<Node>();
        for( Node node : geometry.getNodes() ) {
            List<OsmPrimitive> referrers = node.getReferrers();
            if( node.isNew() && !node.isDeleted() && referrers.size() == 1
                    && referrers.get(0).equals(geometry) && !way.containsNode(node) )
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
        List<Node> geometryNodes = geometry.getNodes();
        for( int i = 0; i < geometryNodes.size(); i++ )
            if( nodeAssoc.containsKey(geometryNodes.get(i)) )
                geometryNodes.set(i, nodeAssoc.get(geometryNodes.get(i)));

        // Now do the replacement
        List<Command> commands = new ArrayList<Command>();
        commands.add(new ChangeNodesCommand(way, geometryNodes));

        // Move old nodes to new positions
        for( Node node : nodeAssoc.keySet() )
            commands.add(new MoveCommand(nodeAssoc.get(node), node.getCoor()));

        // Copy tags from temporary way (source etc.)
        for( String key : geometry.keySet() )
            commands.add(new ChangePropertyCommand(way, key, geometry.get(key)));

        // Remove geometry way from selection
        getCurrentDataSet().clearSelection(geometry);

        // And delete old geometry way
        commands.add(new DeleteCommand(geometry));

        // Delete nodes that are not used anymore
        if( !nodePool.isEmpty() )
            commands.add(new DeleteCommand(nodePool));

        // Two items in undo stack: change original way and delete geometry way
        Main.main.undoRedo.add(new SequenceCommand(
                tr("Replace geometry for way {0}", way.getDisplayName(DefaultNameFormatter.getInstance())),
                commands));
    }

    /**
     * Create a list of nodes that are not used anywhere except in the way.
     *
     * @param way
     * @return 
     */
    protected Collection<Node> getUnimportantNodes(Way way) {
        Set<Node> nodePool = new HashSet<Node>();
        Area a = getCurrentDataSet().getDataSourceArea();
        for (Node n : way.getNodes()) {
            List<OsmPrimitive> referrers = n.getReferrers();
            if (!n.isDeleted() && referrers.size() == 1 && referrers.get(0).equals(way)
                    && (n.isNewOrUndeleted() || a == null || a.contains(n.getCoor()))) {
                nodePool.add(n);
            }
        }
        return nodePool;
    }
    
    /**
     * Find node from the collection which is nearest to <tt>node</tt>. Max distance is taken in consideration.
     * @return null if there is no such node.
     */
    private Node findNearestNode( Node node, Collection<Node> nodes ) {
        if( nodes.contains(node) )
            return node;
        
        Node nearest = null;
        double distance = MAX_NODE_REPLACEMENT_DISTANCE;
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

