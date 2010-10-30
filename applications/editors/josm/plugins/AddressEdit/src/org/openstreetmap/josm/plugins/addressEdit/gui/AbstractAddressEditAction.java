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

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;

public abstract class AbstractAddressEditAction extends JosmAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3080414353417044998L;

	private AddressEditSelectionEvent event;
	protected AddressEditContainer container;

	/**
	 * @param name
	 * @param icon
	 */
	public AbstractAddressEditAction(String name, String iconName, String tooltip) {
		super(name, iconName, tooltip, null, true);
		
		setEnabled(false);
	}

	/**
	 * @param name
	 */
	public AbstractAddressEditAction(String name) {
		this(name, null, "");
	}
	
	/**
	 * @return the container
	 */
	public AddressEditContainer getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(AddressEditContainer container) {
		this.container = container;
		updateEnabledState(container);
	}

	/**
	 * Updates 'enabled' state depending on the given selection event.
	 * @param ev
	 * @return
	 */
	public void updateEnabledState(AddressEditSelectionEvent ev) {
		// If the tree selection changes, we will get a new event. So this is safe.
		super.updateEnabledState();
		this.event = ev; // save for later use.
		if (ev != null) {
			updateEnabledState(ev);
		}
	}

	/**
	 * Updates 'enabled' state depending on the given address container object.
	 * @param container The address container (maybe null).
	 * @return
	 */
	protected abstract void updateEnabledState(AddressEditContainer container);

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (event != null) { // use the event acquired previously.
			addressEditActionPerformed(event);	
			event = null; // consume event
		} else {
			actionPerformed(arg0);
		}
	}
	

	/**
	 * Redirected action handler
	 * @param ev
	 */
	public abstract void addressEditActionPerformed(AddressEditSelectionEvent ev);
	
	

}
