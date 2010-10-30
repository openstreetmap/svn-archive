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
package org.openstreetmap.josm.plugins.addressEdit.gui;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;
import org.openstreetmap.josm.plugins.addressEdit.AddressNode;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

@SuppressWarnings("serial")
public class SelectAddressesInMapAction extends AbstractAddressEditAction {

	public SelectAddressesInMapAction() {
		// we simply use the existing icon :-|
		super(tr("Select in map"), "selectall", "Selects selected addresses in the map");
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		if (ev == null) return;
		
		internalSelectAddresses(ev.getSelectedUnresolvedAddresses());
	}

	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		internalSelectAddresses(container.getUnresolvedAddresses());		
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer)
	 */
	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(container != null && container.getNumberOfIncompleteAddresses() > 0);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	protected void updateEnabledState(AddressEditSelectionEvent event) {
		setEnabled(event != null && event.getSelectedUnresolvedAddresses() != null);
	}

	/**
	 * Internal helper to select the given addresses in the map. 
	 * @param addrToSel
	 */
	private void internalSelectAddresses(List<AddressNode> addrToSel) {
		if (addrToSel == null) return;
		
		List<OsmPrimitive> sel = new ArrayList<OsmPrimitive>();
		
		for (AddressNode aNode : addrToSel) {
			sel.add(aNode.getOsmObject());
		}

		getCurrentDataSet().setSelected(sel);
	}

}
