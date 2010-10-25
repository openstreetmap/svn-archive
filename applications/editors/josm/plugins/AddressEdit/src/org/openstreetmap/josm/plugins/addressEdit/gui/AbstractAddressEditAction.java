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
import javax.swing.Icon;

public abstract class AbstractAddressEditAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3080414353417044998L;

	private AddressSelectionEvent event;

	/**
	 * @param name
	 * @param icon
	 */
	public AbstractAddressEditAction(String name, Icon icon) {
		super(name, icon);
		setEnabled(false);
	}

	/**
	 * @param name
	 */
	public AbstractAddressEditAction(String name) {
		this(name, null);
	}

	/**
	 * Updates 'enabled' state.
	 * @param ev
	 * @return
	 */
	public void updateEnabledState(AddressSelectionEvent ev) {
		// If the tree selection changes, we will get a new event. So this is safe.
		this.event = ev; // save for later use.  
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (event != null) { // use the event acquired previously.
			addressEditActionPerformed(event);			
		}
	}
	

	/**
	 * Redirected action handler
	 * @param ev
	 */
	public abstract void addressEditActionPerformed(AddressSelectionEvent ev);
	
	

}
