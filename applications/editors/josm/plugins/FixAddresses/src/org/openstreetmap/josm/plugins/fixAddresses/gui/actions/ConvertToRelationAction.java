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

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;
import org.openstreetmap.josm.plugins.fixAddresses.TagUtils;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

@SuppressWarnings("serial")
public class ConvertToRelationAction extends AbstractAddressEditAction {

	public ConvertToRelationAction() {
		super(tr("Convert to relation."), "convert2rel_24",
			tr("Create relation between street and related addresses."),
			"fixaddresses/converttorelation");
	}

	/**
	 * Instantiates a new convert to relation action.
	 *
	 * @param name the name of the action
	 * @param iconName the icon name
	 * @param tooltip the tool tip to show on hover
	 */
	public ConvertToRelationAction(String name, String iconName, String tooltip, String toolbar) {
		super(name, iconName, tooltip, toolbar);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		OSMStreet streetNode = ev.getSelectedStreet();

		if (streetNode != null) {
			createRelationForStreet(streetNode);
		}
	}

	/**
	 * Creates the 'associatedStreet' relation for a given street by adding all addresses which
	 * matches the name of the street.
	 *
	 * @param streetNode the street node
	 */
	protected void createRelationForStreet(OSMStreet streetNode) {
		if (streetNode == null || !streetNode.hasAddresses()) return;

		beginTransaction(tr("Create address relation for ") + " '" + streetNode.getName() + "'");
		// Create the relation
		Relation r = new Relation();
		commands.add(new AddCommand(r));
		commands.add(new ChangePropertyCommand(r, TagUtils.NAME_TAG, streetNode.getName()));
		commands.add(new ChangePropertyCommand(r, TagUtils.RELATION_TYPE, TagUtils.ASSOCIATEDSTREET_RELATION_TYPE));
		// add street with role 'street'
		r.addMember(new RelationMember(TagUtils.STREET_RELATION_ROLE, streetNode.getOsmObject()));

		// add address members
		for (OSMAddress addrNode : streetNode.getAddresses()) {
			beginObjectTransaction(addrNode);
			r.addMember(new RelationMember(TagUtils.HOUSE_RELATION_ROLE, addrNode.getOsmObject()));
			addrNode.setStreetName(null); // remove street name
			finishObjectTransaction(addrNode);
		}
		finishTransaction();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		// Nothing to do (yet).
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent)
	 */
	@Override
	protected void updateEnabledState(AddressEditSelectionEvent event) {
		if (event == null) return;

		OSMStreet street = event.getSelectedStreet();
		setEnabled(street != null && street.hasAddresses() && !street.hasAssociatedStreetRelation());
	}

}
