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
import static org.openstreetmap.josm.tools.I18n.trc;

import java.util.Collections;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;

/**
 * Provides a table model to show unresolved addresses.
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */

public class UnresolvedAddressesTableModel extends AddressEditTableModel {

	private static final int NUMBER_OF_COLUMNS = 5;
	private static final String[] COLUMN_NAMES = new String[] { tr("Street"),
			tr("Number"), trc("address", "City" /* fix #8140 */), tr("Postcode"), tr("Name") };

	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
			String.class, String.class, String.class, String.class,
			String.class };

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

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.DefaultTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return NUMBER_OF_COLUMNS;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.DefaultTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (addressContainer == null
				|| addressContainer.getUnresolvedAddresses() == null) {
			return 0;
		}
		return addressContainer.getNumberOfUnresolvedAddresses();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column) {
		OSMAddress aNode = (OSMAddress) getEntityOfRow(row);

		if (aNode == null) {
			return null;
		}

		switch (column) {
		case 0:
			return aNode.getStreetName();
		case 1:
			return aNode.getHouseNumber();
		case 2:
			return aNode.getCity();
		case 3:
			return aNode.getPostalCode();
		case 4:
			return aNode.getName();
		default:
			throw new RuntimeException("Invalid column index: " + column);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int arg0) {
		return COLUMN_CLASSES[arg0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditTableModel
	 * #getEntityOfRow(int)
	 */
	@Override
	public IOSMEntity getEntityOfRow(int row) {
		if (addressContainer == null
				|| addressContainer.getUnresolvedAddresses() == null) {
			return null;
		}
		if (row < 0 || row >= addressContainer.getNumberOfUnresolvedAddresses()) {
			return null;
		}
		return addressContainer.getUnresolvedAddresses().get(row);
	}

	@Override
	public int getRowOfEntity(IOSMEntity entity) {
		if (addressContainer == null
				|| addressContainer.getUnresolvedAddresses() == null) {
			return -1;
		}

		return addressContainer.getUnresolvedAddresses().indexOf(entity);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditTableModel
	 * #sortByColumn(int, boolean)
	 */
	@Override
	protected void sortByColumn(int column, boolean ascending) {
		if (addressContainer.getNumberOfUnresolvedAddresses() == 0)
			return;

		Collections.sort(addressContainer.getUnresolvedAddresses(),
				new UnresolvedAddressModelSorter(column, ascending));
	}

	/**
	 * Internal class StreetModelSorter.
	 */
	class UnresolvedAddressModelSorter extends ColumnSorter<OSMAddress> {

		public UnresolvedAddressModelSorter(int column, boolean asc) {
			super(column, asc);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditTableModel
		 * .ColumnSorter#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(OSMAddress arg0, OSMAddress arg1) {
			int cc = 0;
			switch (getColumn()) {
			case 0:
				cc = arg0.getStreetName().compareTo(arg1.getStreetName());
				break;
			case 1:
				cc = arg0.getHouseNumber().compareTo(arg1.getHouseNumber());
				break;
			case 2:
				cc = arg0.getCity().compareTo(arg1.getCity());
				break;
			case 3:
				cc = arg0.getPostalCode().compareTo(arg1.getPostalCode());
				break;
			case 4:
				cc = arg0.getName().compareTo(arg1.getName());
				break;
			default:
				throw new RuntimeException("Invalid column index: "
						+ getColumn());
			}

			if (!isAscending()) {
				cc = -cc;
			}

			return cc;
		}
	}
}
