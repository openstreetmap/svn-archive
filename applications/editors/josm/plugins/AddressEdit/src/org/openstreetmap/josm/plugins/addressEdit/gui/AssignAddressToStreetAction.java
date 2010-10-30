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
package org.openstreetmap.josm.plugins.addressEdit.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;
import org.openstreetmap.josm.plugins.addressEdit.AddressNode;
import org.openstreetmap.josm.plugins.addressEdit.StreetNode;

public class AssignAddressToStreetAction extends AbstractAddressEditAction {

	public AssignAddressToStreetAction() {
		super(tr("Assign address to street"), "assignstreet_24", "Assign the selected address(es) to the selected street.");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6180491357232121384L;

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {		
		StreetNode streetNode = ev.getSelectedStreet();
		
		
		if (streetNode != null && ev.getSelectedUnresolvedAddresses() != null) {
			for (AddressNode addrNode : ev.getSelectedUnresolvedAddresses()) {
				addrNode.assignStreet(streetNode);
				System.out.println("Assign " + addrNode + " to " + streetNode);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void updateEnabledState(AddressEditSelectionEvent ev) {
		setEnabled(ev.getSelectedStreet() != null && ev.getSelectedUnresolvedAddresses() != null);
	}

	@Override
	public void updateEnabledState(AddressEditContainer container) {
		// we only accept a selection here
		setEnabled(false);
	}


}
