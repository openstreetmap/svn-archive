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

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Extends current selection
 */
public class AdjacentNodesAction extends JosmAction {

    public static final boolean treeMode = false;

    public AdjacentNodesAction() {
        super(tr("Adjacent nodes"), "adjnodes", tr("Select adjacent nodes"),
                Shortcut.registerShortcut("tools:adjnodes", tr("Tool: {0}", "Adjacent nodes"),
                        KeyEvent.VK_E, Shortcut.DIRECT), true);
        putValue("help", ht("/Action/AdjacentNodes"));
    }

    private transient Set<Way> activeWays = new HashSet<>();

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getActiveDataSet();
        Collection<Node> selectedNodes = ds.getSelectedNodes();
        Set<Way> selectedWays = new LinkedHashSet<>(ds.getSelectedWays());

        // if no nodes and no ways are selected, do nothing
        if (selectedNodes.isEmpty() && selectedWays.isEmpty()) return;

        if (selectedWays.isEmpty()) {
            // if one node is selected, use ways connected to it to extend selection
            // activeWays are remembered for next extend action (!!!)

            // FIXME: some strange behaviour is possible if user deletes some of these ways
            // how to clear activeWays during such user actions? Do not know
            if (selectedNodes.size() == 1) {
                activeWays.clear();
            }
        } else {
            // use only ways that were selected for adding nodes
            activeWays = selectedWays;
        }

        // selecting nodes of selected ways
        if (selectedNodes.isEmpty()) {
            Set<Node> newNodes = new LinkedHashSet<>();
            NodeWayUtils.addNodesConnectedToWays(selectedWays, newNodes);
            activeWays.clear();
            ds.setSelected(newNodes);
            return;
        }

        if (activeWays.isEmpty()) {
            NodeWayUtils.addWaysConnectedToNodes(selectedNodes, activeWays);
        }

        Set<Node> newNodes = new LinkedHashSet<>();
        for (Node node: selectedNodes) {
            for (Way w: activeWays) {
                NodeWayUtils.addNeighbours(w, node, newNodes);
            }
        }

        // select only newly found nodes
        newNodes.removeAll(selectedNodes);

        // enable branching on next call of this function
        // if no new nodes were found, next search will include all touched ways
        if (newNodes.isEmpty()) {
            activeWays.clear();
        }

        ds.addSelected(newNodes);
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
