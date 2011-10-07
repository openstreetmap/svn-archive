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
        Set<Way> newWays = new HashSet<Way>();

        if( selectedWays.size() == 1 ) {
            Way firstWay = selectedWays.get(0);
            String key = firstWay.hasKey("name") ? "name" : "ref";
            if( !firstWay.hasKey(key) ) {
                JOptionPane.showMessageDialog(Main.parent, "The selected way should have either name or ref tag.", "Select Highway", JOptionPane.ERROR_MESSAGE);
                return;
            }
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
        } else if( selectedWays.size() == 2 ) {
            JOptionPane.showMessageDialog(Main.parent, "Sorry, two ways are not supported yet", "Select Highway", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            JOptionPane.showMessageDialog(Main.parent, "Please select exactly one way for this action", "Select Highway", JOptionPane.ERROR_MESSAGE);
            return;
        }

        getCurrentDataSet().setSelected(newWays);
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
        int count = 0;
        for( OsmPrimitive p : selection )
            if( p instanceof Way )
                count++;
        setEnabled(count == 1); // todo: or 2
    }
}
