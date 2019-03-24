// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Select last modified ways.
 */
public class SelectModWaysAction extends JosmAction {
    private Command lastCmd;

    public SelectModWaysAction() {
        super(tr("Select last modified ways"), "selmodways",
                tr("Select last modified ways"),
                Shortcut.registerShortcut("tools:selmodways", tr("Tool: {0}", "Select last modified ways"),
                        KeyEvent.VK_Z, Shortcut.ALT_SHIFT), true);
        putValue("help", ht("/Action/SelectLastModifiedWays"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds != null) {
            ds.clearSelection(ds.getSelectedNodes());
            Command cmd;

            if (UndoRedoHandler.getInstance().commands == null) return;
            int num = UndoRedoHandler.getInstance().commands.size();
            if (num == 0) return;
            int k = 0, idx;
            // check if executed again, we cycle through all available commands
            if (lastCmd != null && !ds.getSelectedWays().isEmpty()) {
                idx = UndoRedoHandler.getInstance().commands.lastIndexOf(lastCmd);
            } else {
                idx = num;
            }

            Set<Way> ways = new HashSet<>(10);
            do {  //  select next history element
                if (idx > 0) idx--; else idx = num-1;
                cmd = UndoRedoHandler.getInstance().commands.get(idx);
                if (cmd.getAffectedDataSet() == ds) {
                    Collection<? extends OsmPrimitive> pp = cmd.getParticipatingPrimitives();
                    ways.clear();
                    for (OsmPrimitive p : pp) {
                        // find all affected ways
                        if (p instanceof Way && !p.isDeleted() && !p.isDisabled())
                            ways.add((Way) p);
                    }
                    if (!ways.isEmpty() && !ds.getSelectedWays().containsAll(ways)) {
                        ds.setSelected(ways);
                        lastCmd = cmd; // remember last used command and last selection
                        return;
                    }
                }
                k++;
            } while (k < num); // try to find previous command if this affects nothing
            lastCmd = null;
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditDataSet() != null);
    }
}
