// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Extends current selection by selecting nodes on all touched ways
 */
public class IntersectedWaysAction extends JosmAction {

    public IntersectedWaysAction() {
        super(tr("Intersecting ways"), "intway", tr("Select intersecting ways"),
                Shortcut.registerShortcut("tools:intway", tr("Tool: {0}", "Intersecting ways"),
                        KeyEvent.VK_I, Shortcut.DIRECT), true);
        putValue("help", ht("/Action/SelectIntersectingWays"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        Collection<Way> selectedWays = ds.getSelectedWays();

        // select ways attached to already selected ways
        if (!selectedWays.isEmpty()) {
            Set<Way> newWays = new HashSet<>();
            NodeWayUtils.addWaysIntersectingWays(
                    ds.getWays(),
                    selectedWays, newWays);
            ds.addSelected(newWays);
            return;
        } else {
            new Notification(
                    tr("Please select some ways to find connected and intersecting ways!")
                    ).setIcon(JOptionPane.WARNING_MESSAGE).show();
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
