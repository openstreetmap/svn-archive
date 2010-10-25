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

import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;
import org.openstreetmap.josm.plugins.addressEdit.IAddressEditContainerListener;

public class AddressEditTableModel extends DefaultTableModel implements IAddressEditContainerListener{

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
		fireTableDataChanged();
	}

	@Override
	public void entityChanged() {
		fireTableDataChanged();
	}
}