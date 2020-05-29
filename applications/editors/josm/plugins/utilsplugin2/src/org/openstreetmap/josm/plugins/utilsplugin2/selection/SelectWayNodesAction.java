// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Select all nodes of a selected way.
 */
public class SelectWayNodesAction extends JosmAction {

    /**
     * Create a new SelectWayNodesAction
     */
    public SelectWayNodesAction() {
        super(tr("Select Way Nodes"), "selectwaynodes", tr("Select all nodes of a selected way."),
                Shortcut.registerShortcut("tools:selectwaynodes", tr("Tool: {0}", tr("Select Way Nodes")),
                        KeyEvent.VK_N, Shortcut.CTRL_SHIFT), true);
        putValue("help", ht("/Action/SelectWayNodes"));
    }

    /**
     * Called when the action is executed.
     *
     * This method does some checking on the selection.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Set<Node> selectedNodes = new LinkedHashSet<>();

        for (OsmPrimitive p : getLayerManager().getActiveDataSet().getSelected()) {
            if (p instanceof Way) {
                Way w = (Way) p;
                if (w.isUsable() && w.getNodesCount() > 1) {
                    for (Node n : w.getNodes()) {
                        if (!n.isDisabled()) {
                            selectedNodes.add(n);
                        }
                    }
                }
            } else if (p instanceof Node) {
                selectedNodes.add((Node) p);
            }
        }

        getLayerManager().getActiveDataSet().setSelected(selectedNodes);
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }
}
