// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Use selection history to restore previous selection
 */
public class UndoSelectionAction extends JosmAction {

    public UndoSelectionAction() {
        super(tr("Undo selection"), "undoselection",
                tr("Reselect last added object or selection form history"),
                Shortcut.registerShortcut("tools:undoselection", tr("Tool: {0}", "Undo selection"),
                        KeyEvent.VK_Z, Shortcut.CTRL_SHIFT), true);
        putValue("help", ht("/Action/UndoSelection"));
    }

    private Collection<OsmPrimitive> lastSel;
    private int index;

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds != null) {
            LinkedList<Collection<? extends OsmPrimitive>> history = ds.getSelectionHistory();
            if (history == null || history.isEmpty()) return; // empty history
            if (lastSel != null) {
                Collection<OsmPrimitive> selection = ds.getSelected();
                if (lastSel.size() == selection.size() && selection.containsAll(lastSel)) {
                    // repeated action
                } else {
                    index = -1;
                }
            }

            int num = history.size();
            int k = 0;

            Set<OsmPrimitive> newSel = new HashSet<>();
            while (k < num) {
                if (index+1 < history.size()) index++; else index = 0;
                Collection<? extends OsmPrimitive> histsel = history.get(index);
                // remove deleted entities from selection
                newSel.clear();
                newSel.addAll(histsel);
                newSel.removeIf(p -> p == null || p.isDeleted());
                k++;
                if (!newSel.isEmpty()) {
                    Collection<OsmPrimitive> oldSel = ds.getSelected();
                    if (oldSel.size() == newSel.size() && newSel.containsAll(oldSel)) {
                        // ignore no-change selection
                        continue;
                    }
                    break;
                }
            }

            // set new selection (is added to history)
            ds.setSelected(newSel);
            lastSel = ds.getSelected();
        }
    }

    @Override
    protected void updateEnabledState() {
        DataSet ds = getLayerManager().getEditDataSet();
        lastSel = null;
        index = -1;
        setEnabled(ds != null && ds.getSelectionHistory().isEmpty());
    }
}
