/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/* File created on 30.10.2010 */
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
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer)
	 */
	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	protected void updateEnabledState(AddressEditSelectionEvent event) {
		setEnabled(event != null && event.hasAddresses());
	}

	/**
	 * Internal helper to select the given addresses in the map.
	 * @param addrToSel
	 */
	private void internalSelectAddresses(List<OSMAddress> addrToSel) {
		if (addrToSel == null) return;

		List<OsmPrimitive> sel = new ArrayList<OsmPrimitive>();

		getCurrentDataSet().clearSelection();
		for (OSMAddress aNode : addrToSel) {
			sel.add(aNode.getOsmObject());

			// Select also guessed objects, if wished
			if (FixAddressesPlugin.getPreferences().isSelectGuessedObjects()) {
				for (OsmPrimitive osmPrimitive : aNode.getGuessedObjects()) {
					sel.add(osmPrimitive);
				}
			}
		}

		getCurrentDataSet().setSelected(sel);
	}

}
