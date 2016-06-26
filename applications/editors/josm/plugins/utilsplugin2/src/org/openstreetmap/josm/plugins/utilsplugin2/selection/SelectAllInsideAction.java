// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Extends current selection by selecting nodes on all touched ways
 */
public class SelectAllInsideAction extends JosmAction {

    public SelectAllInsideAction() {
        super(tr("All inside [testing]"), "selinside", tr("Select all inside selected polygons"),
                Shortcut.registerShortcut("tools:selinside", tr("Tool: {0}", "All inside"),
                        KeyEvent.VK_I, Shortcut.ALT_SHIFT), true);
        putValue("help", ht("/Action/SelectAllInside"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        Collection<OsmPrimitive> insideSelected = NodeWayUtils.selectAllInside(ds.getSelected(), ds, true);

        if (!insideSelected.isEmpty()) {
            ds.addSelected(insideSelected);
        } else {
            new Notification(
                    tr("Nothing found. Please select some closed ways or multipolygons to find all primitives inside them!"))
            .setIcon(JOptionPane.WARNING_MESSAGE)
            .show();
        }
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
