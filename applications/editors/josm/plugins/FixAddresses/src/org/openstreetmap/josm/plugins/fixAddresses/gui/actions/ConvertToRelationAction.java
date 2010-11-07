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
		super(tr("Convert to relation."), "convert2rel_24", "Create relation between street and related addresses.");
	}

	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		OSMStreet streetNode = ev.getSelectedStreet();
		
		if (streetNode != null) {
			beginTransaction(tr("Create address relation for ") + " '" + streetNode.getName() + "'");
			
			Relation r = new Relation();
			commands.add(new AddCommand(r));
			commands.add(new ChangePropertyCommand(r, TagUtils.NAME_TAG, streetNode.getName()));
			commands.add(new ChangePropertyCommand(r, TagUtils.RELATION_TYPE, TagUtils.ASSOCIATEDSTREET_RELATION_TYPE));
						
			r.addMember(new RelationMember(TagUtils.STREET_RELATION_ROLE, streetNode.getOsmObject()));
			for (OSMAddress addrNode : streetNode.getAddresses()) {
				beginObjectTransaction(addrNode);				
				r.addMember(new RelationMember(TagUtils.HOUSE_RELATION_ROLE, addrNode.getOsmObject()));
				addrNode.setStreetName(null);
				finishObjectTransaction(addrNode);
			}
			finishTransaction();
		}
	}

	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		// Nothing to do (yet).
	}

	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(false);
	}

	@Override
	protected void updateEnabledState(AddressEditSelectionEvent event) {
		if (event == null) return;
		
		OSMStreet street = event.getSelectedStreet();
		setEnabled(street != null && street.hasAddresses());
	}

}
