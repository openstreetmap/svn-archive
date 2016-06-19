// License: GPL. Copyright 2011 by Alexei Kasatkin and Martin Ždila
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

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
 * Unselects all nodes
 */
public class UnselectNodesAction extends JosmAction {

    /**
     * Constructs a new {@code UnselectNodesAction}.
     */
    public UnselectNodesAction() {
        super(tr("Unselect nodes"), "unsnodes",
                tr("Removes all nodes from selection"),
                Shortcut.registerShortcut("tools:unsnodes", tr("Tool: {0}","Unselect nodes"),
                KeyEvent.VK_U, Shortcut.SHIFT), true);
        putValue("help", ht("/Action/UnselectNodes"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(selection, Node.class);
        getLayerManager().getEditDataSet().clearSelection(selectedNodes);
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
