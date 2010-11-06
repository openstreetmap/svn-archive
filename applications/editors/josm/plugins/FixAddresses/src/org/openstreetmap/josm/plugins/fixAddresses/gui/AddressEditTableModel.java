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

import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;

public abstract class AddressEditTableModel extends DefaultTableModel implements IAddressEditContainerListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 424009321818130586L;
	protected AddressEditContainer addressContainer;

	public AddressEditTableModel(AddressEditContainer addressContainer) {
		super();
		this.addressContainer = addressContainer;
		addressContainer.addChangedListener(this);
	}

	@Override
	public void containerChanged(AddressEditContainer container) {
		fireTableDataChanged(); // update model
	}

	@Override
	public void entityChanged(IOSMEntity entity) {
		int row = getRowOfEntity(entity);
		if (row != -1) { // valid row? -> update model
			System.out.println("Update row " + row);
			fireTableRowsUpdated(row, row);
		} // else we don't do anything
	}
	
	/**
	 * Gets the node entity for the given row or null; if row contains no entity.
	 * @param row The row to get the entity object for.
	 * @return
	 */
	public abstract IOSMEntity getEntityOfRow(int row);
	
	/**
	 * Gets the row for the given node entity or -1; if the model does not contain the entity.
	 * @param entity The entity to get the row for.
	 * @return
	 */
	public abstract int getRowOfEntity(IOSMEntity entity);
}