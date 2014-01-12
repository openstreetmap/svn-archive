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
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Assigns one or more selected addresses to a street, i. e. the name of the street is
 * used as value for the addr:street tag.
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */
public class AssignAddressToStreetAction extends AbstractAddressEditAction  {

	/**
	 * Instantiates a new "assign address to street" action.
	 */
	public AssignAddressToStreetAction() {
		super(tr("Assign address to street"), "assignstreet_24",
			tr("Assign the selected address(es) to the selected street."),
			"fixaddresses/assignaddresstostreet");
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
		OSMStreet streetNode = ev.getSelectedStreet();

		if (streetNode != null && ev.getSelectedUnresolvedAddresses() != null) {
			beginTransaction(tr("Set street name") + " '" + streetNode.getName() + "'");
			for (OSMAddress addrNode : ev.getSelectedUnresolvedAddresses()) {
				beginObjectTransaction(addrNode);
				addrNode.assignStreet(streetNode);
				finishObjectTransaction(addrNode);
			}
			finishTransaction();
		}

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void updateEnabledState(AddressEditSelectionEvent ev) {
		setEnabled(ev.getSelectedStreet() != null && ev.hasUnresolvedAddresses());
	}

	@Override
	public void updateEnabledState(AddressEditContainer container) {
		// we only accept a selection here
		setEnabled(false);
	}

	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		// we only accept a selection: nothing to do here
	}


}
