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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openstreetmap.josm.plugins.addressEdit.AddressNode;
import org.openstreetmap.josm.plugins.addressEdit.StreetNode;
import static org.openstreetmap.josm.tools.I18n.tr;

public class AssignAddressToStreetAction extends AbstractAddressEditAction {

	public AssignAddressToStreetAction() {
		super(tr("Assign address to street"));
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6180491357232121384L;

	@Override
	public void addressEditActionPerformed(AddressSelectionEvent ev) {
		DefaultMutableTreeNode streetNode = ev.getSelectedStreet();
		StreetNode sNode = null;
		AddressNode aNode = null;
		
		if (streetNode != null) {
			sNode = (StreetNode) streetNode.getUserObject();
		}
		
		DefaultMutableTreeNode addrNode = ev.getSelectedUnresolvedAddress();
		if (addrNode != null) {
			aNode = (AddressNode) addrNode.getUserObject();
		}
		
		if (sNode != null && aNode != null) {
			System.out.println("Assign " + aNode + " top " + sNode);
			
			aNode.assignStreet(sNode);
			addrNode.removeFromParent();			
		}		
	}

	@Override
	public void updateEnabledState(AddressSelectionEvent ev) {
		super.updateEnabledState(ev);
		setEnabled(ev.getSelectedStreet() != null && ev.getSelectedUnresolvedAddress() != null);
	}


}
