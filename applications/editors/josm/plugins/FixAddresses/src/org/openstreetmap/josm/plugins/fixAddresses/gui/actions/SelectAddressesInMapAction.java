// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.FixAddressesPlugin;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Marks selected addresses in the map.
 *
 * @author Oliver Wieland &lt;oliver.wieland@online.de>
 */
@SuppressWarnings("serial")
public class SelectAddressesInMapAction extends AbstractAddressEditAction {

    /**
     * Instantiates a new "select addresses in map" action.
     */
    public SelectAddressesInMapAction() {
        super(tr("Select"), "selectall", tr("Marks selected addresses in the map"),
            "fixaddresses/selectaddressesinmap");
    }

    @Override
    public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
        if (ev == null) return;

        if (ev.hasUnresolvedAddresses()) {
            internalSelectAddresses(ev.getSelectedUnresolvedAddresses());
        } else if (ev.hasIncompleteAddresses()) {
            internalSelectAddresses(ev.getSelectedIncompleteAddresses());
        }
    }

    @Override
    public void addressEditActionPerformed(AddressEditContainer container) {
        // do nothing
    }

    @Override
    protected void updateEnabledState(AddressEditContainer container) {
        setEnabled(false);
    }

    @Override
    protected void updateEnabledState(AddressEditSelectionEvent event) {
        setEnabled(event != null && event.hasAddresses());
    }

    /**
     * Internal helper to select the given addresses in the map.
     * @param addrToSel addresses
     */
    private void internalSelectAddresses(List<OSMAddress> addrToSel) {
        if (addrToSel == null) return;

        List<OsmPrimitive> sel = new ArrayList<>();

        getLayerManager().getEditDataSet().clearSelection();
        for (OSMAddress aNode : addrToSel) {
            sel.add(aNode.getOsmObject());

            // Select also guessed objects, if wished
            if (FixAddressesPlugin.getPreferences().isSelectGuessedObjects()) {
                for (OsmPrimitive osmPrimitive : aNode.getGuessedObjects()) {
                    sel.add(osmPrimitive);
                }
            }
        }

        getLayerManager().getEditDataSet().setSelected(sel);
    }

}
