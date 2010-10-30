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

/* File created on 25.10.2010 */
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.AddressNode;
import org.openstreetmap.josm.plugins.fixAddresses.INodeEntity;

/**
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

public class UnresolvedAddressesTableModel extends AddressEditTableModel {

	private static final int NUMBER_OF_COLUMNS = 5;
	private static final String[] COLUMN_NAMES = new String[]{
		tr("Street"), tr("Housenumber"), tr("City"), tr("Postcode"), tr("Name")};
	
	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
		String.class, String.class, String.class, String.class, String.class};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 424009321818130586L;
	
	/**
	 * @param addressContainer
	 */
	public UnresolvedAddressesTableModel(AddressEditContainer addressContainer) {
		super(addressContainer);
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return NUMBER_OF_COLUMNS;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public int getRowCount() {
		if (addressContainer == null || addressContainer.getUnresolvedAddresses() == null) {
			return 0;
		}
		return addressContainer.getNumberOfUnresolvedAddresses();
	}

	@Override
	public Object getValueAt(int row, int column) {
		AddressNode aNode = (AddressNode) getEntityOfRow(row);
		
		if (aNode == null) {
			return null;
		}
		
		switch (column) {
		case 0:
			String guessed = aNode.getGuessedStreetName();
			String cur = aNode.getStreet();
			if (aNode.hasGuessedStreetName() && AddressNode.MISSING_TAG.equals(cur)) {				
				return "*" + guessed;
			} else {
				return aNode.getStreet();
			}
		case 1:
			return aNode.getHouseNumber();
		case 2:
			return aNode.getCity();
		case 3:
			return aNode.getPostCode();
		case 4:
			return aNode.getName();			
		default:
			throw new RuntimeException("Invalid column index: " + column);
		}
		
	}
	
	@Override
	public Class<?> getColumnClass(int arg0) {
		return COLUMN_CLASSES[arg0];
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public INodeEntity getEntityOfRow(int row) {
		if (addressContainer == null || addressContainer.getUnresolvedAddresses() == null) {
			return null;
		}
		if (row < 0 || row > addressContainer.getNumberOfUnresolvedAddresses()) {
			return null;
		}
		return addressContainer.getUnresolvedAddresses().get(row);	
	}
}
