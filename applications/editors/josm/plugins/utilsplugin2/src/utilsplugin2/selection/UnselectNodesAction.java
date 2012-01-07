// License: GPL. Copyright 2011 by Alexei Kasatkin and Martin Ždila
package utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Set;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Unselects all nodes
 */
public class UnselectNodesAction extends JosmAction {

    
    public UnselectNodesAction() {
        super(tr("Unselect nodes"), "unsnodes",
                tr("Removes all nodes from selection"),
                Shortcut.registerShortcut("tools:unsnodes", tr("Tool: {0}","Unselect nodes"),
                KeyEvent.VK_U, Shortcut.GROUP_EDIT, KeyEvent.ALT_DOWN_MASK), true);
        putValue("help", ht("/Action/UnselectNodes"));
    }

    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(selection, Node.class);
        getCurrentDataSet().clearSelection(selectedNodes);
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
