package utilsplugin2.dumbutils;

import java.awt.event.KeyEvent;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import java.util.*;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.actions.JosmAction;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Pastes relation membership from objects in the paste buffer onto selected object(s).
 *
 * @author Zverik
 */
public class AlignWayNodesAction extends JosmAction {
    private static final String TITLE = tr("Align Way Nodes");
    private static final double MOVE_THRESHOLD = 1e-9;

    public AlignWayNodesAction() {
        super(TITLE, "dumbutils/alignwaynodes", tr("Align nodes in a way"),
                Shortcut.registerShortcut("tools:alignwaynodes", tr("Tool: {0}", tr("Align Way Nodes")), KeyEvent.VK_L, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT)
                , true);
    }

    public void actionPerformed( ActionEvent e ) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Node> selectedNodes = filterNodes(selection);
        int selectedNodesCount = selectedNodes.size();
        Set<Way> ways = findCommonWays(selectedNodes);
        if( ways == null || ways.size() != 1 || selectedNodesCount == 0 )
            return;
        Way way = ways.iterator().next();
        if( way.getNodesCount() < (way.isClosed() ? 4 : 3) ) {
            JOptionPane.showMessageDialog(Main.parent, tr("The way with selected nodes can not be straightened."), TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prepare a list of nodes to align
        int firstNodePos = findFirstNode(way, selectedNodes);
        int lastNodePos = way.isClosed() ? firstNodePos : way.getNodesCount();
        List<Node> nodes = new ArrayList<Node>();
        int i = firstNodePos;
        boolean iterated = false;
        while( !iterated || i != lastNodePos ) {
            Node node = way.getNode(i);
            if( selectedNodes.contains(node) ) {
                nodes.add(node);
                selectedNodes.remove(node);
                if( selectedNodesCount == 1 ) {
                    nodes.add(0, way.getNode( i > 0 ? i - 1 : way.isClosed() ? way.getNodesCount() - 2 : i + 2 ));
                    nodes.add(way.getNode( i + 1 < way.getNodesCount() ? i + 1 : way.isClosed() ? 1 : i - 2 ));
                }
                if( selectedNodes.isEmpty() )
                    break;
            } else if( selectedNodesCount == 2 && selectedNodes.size() == 1 )
                nodes.add(node);
            i++;
            if( i >= way.getNodesCount() && way.isClosed() )
                i = 0;
            iterated = true;
        }

        if( nodes.size() < 3 ) {
            JOptionPane.showMessageDialog(Main.parent, tr("Internal error: number of nodes is {0}.", nodes.size()),
                    TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Now, we have an ordered list of nodes, of which idx 0 and N-1 serve as guides
        // and 1..N-2 should be aligned with them
        List<Command> commands = new ArrayList<Command>();
        double ax = nodes.get(0).getEastNorth().east();
        double ay = nodes.get(0).getEastNorth().north();
        double bx = nodes.get(nodes.size() - 1).getEastNorth().east();
        double by = nodes.get(nodes.size() - 1).getEastNorth().north();
        
        for( i = 1; i + 1 < nodes.size(); i++ ) {
            Node n = nodes.get(i);
            
            // Algorithm is copied from org.openstreetmap.josm.actions.AlignInLineAction
            double nx = n.getEastNorth().east();
            double ny = n.getEastNorth().north();

            if( ax == bx ) {
                // Special case if AB is vertical...
                nx = ax;
            } else if( ay == by ) {
                // ...or horizontal
                ny = ay;
            } else {
                // Otherwise calculate position by solving y=mx+c (simplified)
                double m1 = (by - ay) / (bx - ax);
                double c1 = ay - (ax * m1);
                double m2 = (-1) / m1;
                double c2 = ny - (nx * m2);

                nx = (c2 - c1) / (m1 - m2);
                ny = (m1 * nx) + c1;
            }

            // Add the command to move the node to its new position.
            if( Math.abs(nx - n.getEastNorth().east()) > MOVE_THRESHOLD && Math.abs(ny - n.getEastNorth().north()) > MOVE_THRESHOLD )
                commands.add(new MoveCommand(n, nx - n.getEastNorth().east(), ny - n.getEastNorth().north()));
        }

        if( !commands.isEmpty() )
            Main.main.undoRedo.add(new SequenceCommand(TITLE, commands));
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
        Set<Node> nodes = filterNodes(selection);
        Set<Way> ways = findCommonWays(nodes);
        setEnabled(ways != null && ways.size() == 1 && !nodes.isEmpty());
    }

    private Set<Way> findCommonWays( Set<Node> nodes ) {
        Set<Way> ways = null;
        for( Node n : nodes ) {
            List<Way> referrers = OsmPrimitive.getFilteredList(n.getReferrers(), Way.class);
            if( ways == null )
                ways = new HashSet<Way>(referrers);
            else {
                ways.retainAll(referrers);
            }
        }
        return ways;
    }

    private Set<Node> filterNodes( Collection<? extends OsmPrimitive> selection ) {
        Set<Node> result = new HashSet<Node>();
        if( selection != null ) {
            for( OsmPrimitive p : selection )
                if( p instanceof Node )
                    result.add((Node)p);
        }
        return result;
    }

    /**
     * Find the largest empty span between nodes and returns the index of the node right after it.
     *
     * TODO: not the maximum node count, but maximum distance!
     */
    private int findFirstNode( Way way, Set<Node> nodes ) {
        int pos = 0;
        while( pos < way.getNodesCount() && !nodes.contains(way.getNode(pos)) )
            pos++;
        if( pos >= way.getNodesCount() )
            return 0;
        if( !way.isClosed() || nodes.size() <= 1 )
            return pos;

        // now, way is closed
        boolean fullCircle = false;
        int maxLength = 0;
        int lastPos = 0;
        while( !fullCircle ) {
            int length = 0;
            boolean skippedFirst = false;
            while( !(skippedFirst && nodes.contains(way.getNode(pos))) ) {
                skippedFirst = true;
                length++;
                pos++;
                if( pos >= way.getNodesCount() ) {
                    pos = 0;
                    fullCircle = true;
                }
            }
            if( length > maxLength ) {
                maxLength = length;
                lastPos = pos;
            }
        }
        return lastPos;
    }
}
