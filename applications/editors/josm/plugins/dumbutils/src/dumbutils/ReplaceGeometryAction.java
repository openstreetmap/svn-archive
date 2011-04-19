package dumbutils;

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
 * Replaces already existing way with the other, fresh created. Select both ways and push the button.
 *
 * @author Zverik
 */
class ReplaceGeometryAction extends JosmAction {
    private static final String TITLE = "Replace geometry";
    private static final double MAX_NODE_REPLACEMENT_DISTANCE = 3e-4;

    public ReplaceGeometryAction() {
        super(tr(TITLE), "replacegeometry", tr("Replace geometry of selected way with a new one"),
                Shortcut.registerShortcut("tools:replacegeometry", tr(TITLE), KeyEvent.VK_G, Shortcut.GROUP_HOTKEY), true);
    }

    public void actionPerformed( ActionEvent e ) {
        // There must be two ways selected: one with id > 0 and one new.
        List<Way> selection = OsmPrimitive.getFilteredList(getCurrentDataSet().getSelected(), Way.class);
        if( selection.size() != 2 ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("This tool replaces geometry of one way with another, and requires two ways to be selected."),
                    tr(TITLE), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int idxNew = selection.get(0).isNew() ? 0 : 1;
        Way geometry = selection.get(idxNew);
        Way way = selection.get(1 - idxNew);
        if( way.isNew() || !geometry.isNew() ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please select one way that exists in the database and one new way with correct geometry."),
                    tr(TITLE), JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prepare a list of nodes that are not used anywhere except in the way
        Set<Node> nodePool = new HashSet<Node>();
        Area a = getCurrentDataSet().getDataSourceArea();
        for( Node node : way.getNodes() ) {
            List<OsmPrimitive> referrers = node.getReferrers();
            if( !node.isDeleted() && referrers.size() == 1 && referrers.get(0).equals(way)
                    && (node.isNewOrUndeleted() || a.contains(node.getCoor())) )
                nodePool.add(node);
        }

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
}

