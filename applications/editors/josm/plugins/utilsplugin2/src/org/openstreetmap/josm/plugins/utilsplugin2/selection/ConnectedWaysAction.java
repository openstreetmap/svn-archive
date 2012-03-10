// License: GPL. Copyright 2011 by Alexei Kasatkin
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Extends current selection by selecting nodes on all touched ways
 */
public class ConnectedWaysAction extends JosmAction {

    public ConnectedWaysAction() {
        super(tr("All connected ways"), "adjwaysall", tr("Select all connected ways"),
                Shortcut.registerShortcut("tools:adjwaysall", tr("Tool: {0}","All connected ways"),
                KeyEvent.VK_E, Shortcut.CTRL_SHIFT), true);
        putValue("help", ht("/Action/SelectConnectedWays"));
    }

    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(selection, Node.class);
        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Way.class);

        Set<Way> newWays = new HashSet<Way>();

        // selecting ways attached to selected nodes
        if(!selectedNodes.isEmpty()) {
            NodeWayUtils.addWaysConnectedToNodes(selectedNodes, newWays);
        }

        // select ways attached to already selected ways
        newWays.addAll(selectedWays);
        NodeWayUtils.addWaysConnectedToWaysRecursively(selectedWays, newWays);
        
//        System.out.printf("%d ways added to selection\n",newWays.size()-selectedWays.size());
        getCurrentDataSet().setSelected(newWays);

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
