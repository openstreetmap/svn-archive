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

import java.util.Collections;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;

@SuppressWarnings("serial")
public class StreetTableModel extends AddressEditTableModel {
	private static final int NUMBER_OF_COLUMNS = 3;
	private static final String[] COLUMN_NAMES = new String[]{tr("Type"), tr("Name"), tr("Addresses")};
	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{String.class, String.class, Integer.class};
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
		OSMStreet sNode = (OSMStreet) getEntityOfRow(row);

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
		case 4:
			return sNode.hasAssociatedStreetRelation();
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
	public IOSMEntity getEntityOfRow(int row) {
		if (addressContainer == null || addressContainer.getStreetList() == null) {
			return null;
		}
		if (row < 0 || row >= addressContainer.getNumberOfStreets()) {
			return null;
		}
		return addressContainer.getStreetList().get(row);
	}

	@Override
	public int getRowOfEntity(IOSMEntity entity) {
		if (addressContainer == null || addressContainer.getStreetList() == null) {
			return -1;
		}

		return addressContainer.getStreetList().indexOf(entity);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditTableModel#sortByColumn(int, boolean)
	 */
	@Override
	protected void sortByColumn(int column, boolean ascending) {
		Collections.sort(addressContainer.getStreetList(), new StreetModelSorter(column, ascending));
	}

	/**
	 * Internal class StreetModelSorter.
	 */
	class StreetModelSorter extends ColumnSorter<OSMStreet> {

		public StreetModelSorter(int column, boolean asc) {
			super(column, asc);
		}

		public int compare(OSMStreet arg0, OSMStreet arg1) {
			if (arg0 == null || arg1 == null) return 0;

			switch (getColumn()) {
			case 0:
				if (arg0.getType() != null) {
					return arg0.getType().compareTo(arg1.getType());
				} else {
					return arg1.hasName() ? -1 : 0;
				}
			case 1:
				if (arg0.hasName()) {
					return arg0.getName().compareTo(arg1.getName());
				} else {
					return arg1.hasName() ? -1 : 0;
				}
			case 2:
				return new Integer(arg0.getNumberOfAddresses()).
								compareTo(new Integer(arg1.getNumberOfAddresses()));
			default:
			}
			return 0;
		}
	}
}
