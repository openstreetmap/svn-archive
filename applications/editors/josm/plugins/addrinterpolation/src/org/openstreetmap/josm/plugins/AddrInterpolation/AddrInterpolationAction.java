// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.AddrInterpolation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Handy Address Interpolation Functions
 */
public class AddrInterpolationAction extends JosmAction implements DataSelectionListener {

    public AddrInterpolationAction() {
        super(tr("Address Interpolation"), "AddrInterpolation", tr("Handy Address Interpolation Functions"),
                Shortcut.registerShortcut("tools:AddressInterpolation", tr("Tool: {0}", tr("Address Interpolation")),
                        KeyEvent.VK_Z, Shortcut.ALT_CTRL), false);
        setEnabled(false);
        SelectionEventManager.getInstance().addSelectionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        /*AddrInterpolationDialog addrDialog =*/ new AddrInterpolationDialog(tr("Define Address Interpolation"));
    }

    @Override
    public void selectionChanged(SelectionChangeEvent event) {

        for (OsmPrimitive osm : event.getSelection()) {
            if (osm instanceof Way) {
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);
    }
}
