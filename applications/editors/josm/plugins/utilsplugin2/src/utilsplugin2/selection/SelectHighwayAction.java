// License: PD
package utilsplugin2.selection;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Select all connected ways for a street if one way is selected (determine by name/ref),
 * select highway ways between two selected ways.
 * 
 * @author zverik
 */
public class SelectHighwayAction extends JosmAction {

    public SelectHighwayAction() {
        super(tr("Select Highway"), "selecthighway", tr("Select highway for the name/ref given"),
                Shortcut.registerShortcut("tools:selecthighway", tr("Tool: {0}","Select Highway"),
                KeyEvent.VK_W, Shortcut.GROUP_MENU, Shortcut.SHIFT_DEFAULT), true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        List<Way> selectedWays = OsmPrimitive.getFilteredList(getCurrentDataSet().getSelected(), Way.class);

        if( selectedWays.size() == 1 ) {
            getCurrentDataSet().setSelected(selectNamedRoad(selectedWays.get(0)));
        } else if( selectedWays.size() == 2 ) {
            getCurrentDataSet().setSelected(selectHighwayBetween(selectedWays.get(0), selectedWays.get(1)));
        } else {
            JOptionPane.showMessageDialog(Main.parent, "Please select one or two ways for this action", "Select Highway", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Set<Way> selectNamedRoad( Way firstWay ) {
        Set<Way> newWays = new HashSet<Way>();
        String key = firstWay.hasKey("name") ? "name" : "ref";
        if( firstWay.hasKey(key) ) {
            String value = firstWay.get(key);
            Queue<Node> nodeQueue = new LinkedList<Node>();
            nodeQueue.add(firstWay.firstNode());
            while( !nodeQueue.isEmpty() ) {
                Node node = nodeQueue.remove();
                for( Way p : OsmPrimitive.getFilteredList(node.getReferrers(), Way.class) ) {
                    if( !newWays.contains(p) && p.hasKey(key) && p.get(key).equals(value) ) {
                        newWays.add(p);
                        nodeQueue.add(p.firstNode().equals(node) ? p.lastNode() : p.firstNode());
                    }
                }
            }
        }
        return newWays;
    }
    
    private Set<Way> selectHighwayBetween( Way firstWay, Way lastWay ) {
        int minRank = Math.min(getHighwayRank(firstWay), getHighwayRank(lastWay));
        Set<Way> newWays = new HashSet<Way>();
        // todo: make two trees and expand them until they touch.
        // do not lower highway rank!
        JOptionPane.showMessageDialog(Main.parent, "Sorry, two ways are not supported yet", "Select Highway", JOptionPane.ERROR_MESSAGE);
        newWays.add(lastWay);
        newWays.add(firstWay);
        return newWays;
    }
    
    private int getHighwayRank( OsmPrimitive way ) {
        if( !way.hasKey("highway") )
            return 0;
        String highway = way.get("highway");
        if( highway.equals("path") || highway.equals("footway") || highway.equals("cycleway") )
            return 1;
        else if( highway.equals("track") || highway.equals("service") )
            return 2;
        else if( highway.equals("unclassified") || highway.equals("residential") )
            return 3;
        else if( highway.equals("tertiary") || highway.equals("tertiary_link") )
            return 4;
        else if( highway.equals("secondary") || highway.equals("secondary_link") )
            return 5;
        else if( highway.equals("primary") || highway.equals("primary_link") )
            return 6;
        else if( highway.equals("trunk") || highway.equals("trunk_link") || highway.equals("motorway") || highway.equals("motorway_link") )
            return 7;
        return 0;
    }
    
    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null)
            setEnabled(false);
        else
            updateEnabledState(getCurrentDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        int count = 0, rank = 100;
        for( OsmPrimitive p : selection ) {
            if( p instanceof Way ) {
                count++;
                rank = Math.min(rank, getHighwayRank(p));
            }
        }
        setEnabled(count == 1 || (count == 2 && rank > 0));
    }
}
