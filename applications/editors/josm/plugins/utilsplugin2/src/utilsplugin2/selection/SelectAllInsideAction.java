// License: GPL. Copyright 2011 by Alexei Kasatkin
package utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Extends current selection by selecting nodes on all touched ways
 */
public class SelectAllInsideAction extends JosmAction {

    public SelectAllInsideAction() {
        super(tr("All inside [testing]"), "selinside", tr("Select all inside selected polygons"),
                Shortcut.registerShortcut("tools:selinside", tr("Tool: {0}","All inside"),
                KeyEvent.VK_I, Shortcut.GROUP_EDIT ,KeyEvent.ALT_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK), true);
        putValue("help", ht("/Action/SelectAllInside"));
    }

    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(selection, Node.class);
        Set<Way> activeWays = new HashSet<Way>();

        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Way.class);

        // select ways attached to already selected ways
        if (!selectedWays.isEmpty()) {
            Set<Way> newWays = new HashSet<Way>();
            Set<Node> newNodes = new HashSet<Node>();
            for (Way w: selectedWays) {
                NodeWayUtils.addAllInsideWay(getCurrentDataSet(),w,newWays,newNodes);
            }
            getCurrentDataSet().addSelected(newWays);
            getCurrentDataSet().addSelected(newNodes);
            return;
        } else {
             JOptionPane.showMessageDialog(Main.parent,
               tr("Please select some ways to find connected and intersecting ways!"),
               tr("Warning"), JOptionPane.WARNING_MESSAGE);
        }

    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        setEnabled(!selection.isEmpty());
    }


}
