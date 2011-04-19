package dumbutils;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.RelationData;
import org.openstreetmap.josm.data.osm.PrimitiveData;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import java.util.*;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.tools.Shortcut;
import java.awt.event.ActionEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Pastes relation membership from objects in the paste buffer onto selected object(s).
 *
 * @author Zverik
 */
class AlignWayNodesAction extends JosmAction {
    private static final String TITLE = "Align way nodes";

    public AlignWayNodesAction() {
        super(tr(TITLE), "alignwaynodes", tr("Align nodes in a way"), null, true);
    }

    public void actionPerformed( ActionEvent e ) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Node> selectedNodes = filterNodes(selection);
        Set<Way> ways = findCommonWays(selectedNodes);
        if( ways == null || ways.size() != 1 )
            return;
        Way way = ways.iterator().next();

        // Prepare a list of nodes to align
        List<Node> nodes = new ArrayList<Node>();
        for( int i = 0; i < way.getNodesCount(); i++ ) {
            Node node = way.getNode(i);
            if( selectedNodes.contains(node) ) {
                nodes.add(node);
                selectedNodes.remove(node);
            }
            // todo: 1 node - add adjacent; 2 nodes - add all between them
        }

        List<Command> commands = new ArrayList<Command>();
        if( !commands.isEmpty() )
            Main.main.undoRedo.add(new SequenceCommand(tr(TITLE), commands));
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
        Set<Way> ways = findCommonWays(filterNodes(selection));
        setEnabled(ways != null && ways.size() == 1);
    }

    private Set<Way> findCommonWays( Set<Node> nodes ) {
        Set<Way> ways = null;
        for( Node n : nodes ) {
            List<Way> referrers = OsmPrimitive.getFilteredList(n.getReferrers(), Way.class);
            if( ways == null )
                ways = new HashSet<Way>(referrers);
            else {
                if( !ways.containsAll(referrers) )
                    return null;
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
}
