// License: GPL. Copyright 2011 by Alexei Kasatkin ond others
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
public class IntersectedWaysRecursiveAction extends JosmAction {
    
    public IntersectedWaysRecursiveAction() {
        super(tr("All intersecting ways"), "intwayall", tr("Select all intersecting ways"),
                Shortcut.registerShortcut("tools:intwayall", tr("Tool: {0}","All intersecting ways"),
                KeyEvent.VK_I, Shortcut.GROUP_MENU, KeyEvent.CTRL_DOWN_MASK|KeyEvent.ALT_DOWN_MASK), true);
        putValue("help", ht("/Action/SelectAllIntersectingWays"));

    }

    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        
        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Way.class);

        if (!selectedWays.isEmpty()) {
            Set<Way> newWays = new HashSet<Way>();
            NodeWayUtils.addWaysIntersectingWaysRecursively(
                    getCurrentDataSet().getWays(),
                    selectedWays, newWays);
            getCurrentDataSet().addSelected(newWays);
            return;
        } else {
             JOptionPane.showMessageDialog(Main.parent,
               tr("Please select some ways to find all connected and intersecting ways!"),
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
