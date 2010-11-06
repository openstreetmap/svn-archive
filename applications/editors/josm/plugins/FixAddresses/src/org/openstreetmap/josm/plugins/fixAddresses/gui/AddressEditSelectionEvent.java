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
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.StreetNode;

public class AddressEditSelectionEvent extends ActionEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -93034483427803409L;
	private JTable streetTable;
	private JTable unresolvedAddressTable;
	private AddressEditContainer addressContainer;
	
	/**
	 * Creates a new 'AddressEditSelectionEvent'.
	 * @param source The event source.
	 * @param selStreet The street table component.
	 * @param unresolvedAddr The unresolved addresses table component.
	 * @param incomplete The incomplete addresses table component.
	 * @param container The address container instance holding the entities for streets and addresses.
	 */
	public AddressEditSelectionEvent(Object source, JTable selStreet, JTable unresolvedAddr, AddressEditContainer container) {
		super(source, -1, "");
		this.streetTable = selStreet;
		this.unresolvedAddressTable = unresolvedAddr;
		this.addressContainer = container;
	}
	
	/**
	 * Gets the street table component.
	 * @return
	 */
	public JTable getStreetTable() {
		return streetTable;
	}

	/**
	 * Gets the 'unresolved addresses' table component.
	 * @return
	 */
	public JTable getUnresolvedAddressTable() {
		return unresolvedAddressTable;
	}

	public AddressEditContainer getAddressContainer() {
		return addressContainer;
	}
	
	/**
	 * Gets the selected street of the street table.
	 * @return
	 */
	public StreetNode getSelectedStreet() {
		if (streetTable != null && addressContainer != null && addressContainer.getStreetList() != null) {
			int selRows = streetTable.getSelectedRow();
			
			if (selRows < 0 || selRows >= addressContainer.getNumberOfStreets()) {
				return null;
			}
			
			return addressContainer.getStreetList().get(selRows);
		}
		return null;
	}
	
	/**
	 * Gets the list containing the selected items of the 'unresolved addresses ' table.
	 * @return
	 */
	public List<OSMAddress> getSelectedUnresolvedAddresses() {
		if (unresolvedAddressTable != null && 
				addressContainer != null && 
				addressContainer.getUnresolvedAddresses() != null) {
			
			int[] selRows = unresolvedAddressTable.getSelectedRows();
			
			List<OSMAddress> nodes = new ArrayList<OSMAddress>();
			for (int i = 0; i < selRows.length; i++) {
				if (selRows[i] >= 0 && selRows[i] < addressContainer.getNumberOfUnresolvedAddresses()) {
					nodes.add(addressContainer.getUnresolvedAddresses().get(selRows[i]));
				}
			}
			return nodes;
		}
		return null;
	}
}
