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

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;
import org.openstreetmap.josm.plugins.addressEdit.INodeEntity;
import org.openstreetmap.josm.plugins.addressEdit.StreetNode;

public class StreetTableModel extends AddressEditTableModel {

	private static final int NUMBER_OF_COLUMNS = 4;
	private static final String[] COLUMN_NAMES = new String[]{tr("Type"), tr("Name"), tr("Segments"), tr("Addresses")}; 
	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{String.class, String.class, Integer.class, Integer.class};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 424009321818130586L;

	/**
	 * @param addressContainer
	 */
	public StreetTableModel(AddressEditContainer addressContainer) {
		super(addressContainer);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return NUMBER_OF_COLUMNS;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASSES[columnIndex];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (addressContainer == null || addressContainer.getStreetList() == null) {
			return 0;
		}
		return addressContainer.getNumberOfStreets();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column) {		
		StreetNode sNode = (StreetNode) getEntityOfRow(row);
		
		if (sNode == null) {
			return null;
		}
		
		switch (column) {
		case 0:
			return sNode.getType();
		case 1:
			return sNode.getName();
		case 2:
			return sNode.getNumberOfSegments();
		case 3:
			return sNode.getNumberOfAddresses();
		default:
			throw new RuntimeException("Invalid column index: " + column);
		}
		
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public INodeEntity getEntityOfRow(int row) {
		if (addressContainer == null || addressContainer.getStreetList() == null) {
			return null;
		}
		if (row < 0 || row > addressContainer.getNumberOfStreets()) {
			return null;
		}
		return addressContainer.getStreetList().get(row);	
	}
}
