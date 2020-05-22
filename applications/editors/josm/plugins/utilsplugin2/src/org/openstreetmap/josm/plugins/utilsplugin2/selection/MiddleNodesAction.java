// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Selects nodes between two selected
 */
public class MiddleNodesAction extends JosmAction {

    public static final boolean treeMode = false;

    public MiddleNodesAction() {
        super(tr("Middle nodes"), "midnodes", tr("Select middle nodes"),
                Shortcut.registerShortcut("tools:midnodes", tr("Tool: {0}", "Middle nodes"), KeyEvent.VK_E, Shortcut.ALT_SHIFT), true);
        putValue("help", ht("/Action/MiddleNodes"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Set<Node> selectedNodes = new HashSet<>(getLayerManager().getEditDataSet().getSelectedNodes());

        // if no 2 nodes and no ways are selected, do nothing
        if (selectedNodes.size() != 2) {
            new Notification(
                    tr("Please select two nodes connected by way!")
                    ).setIcon(JOptionPane.WARNING_MESSAGE).show();
            return;
        }

        Set<Node> newSelectedNodes = new LinkedHashSet<>();
        NodeWayUtils.addMiddle(selectedNodes, newSelectedNodes);

        // make sure that selected nodes are in the wanted order (see #josm17258)
        getLayerManager().getEditDataSet().clearSelection(newSelectedNodes);
        getLayerManager().getEditDataSet().addSelected(newSelectedNodes);
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
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
