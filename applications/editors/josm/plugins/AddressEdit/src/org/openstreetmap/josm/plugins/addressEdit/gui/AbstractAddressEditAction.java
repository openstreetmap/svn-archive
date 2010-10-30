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

/**
 * Base class for all address related action. An action can work as well on all addresses collected by the
 * container or on the active selection.
 * By default, the action is disabled and the updateEnabledState(...) have to be implemented by
 * subclasses. There are also two separate <tt>actionPerformedXX</tt> methods to do the action on
 * container or on selection items.
 * Most actions will work in both cases, so it is recommended to have one single method which
 * accepts a list of addresses or streets and executes the tasks to be done by this action. 
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

@SuppressWarnings("serial")
public abstract class AbstractAddressEditAction extends JosmAction {
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
		updateEnabledState();
	}

	/**
	 * @return the event
	 */
	protected AddressEditSelectionEvent getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	protected void setEvent(AddressEditSelectionEvent event) {
		this.event = event;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.actions.JosmAction#updateEnabledState()
	 */
	@Override
	protected void updateEnabledState() {
		if (this.event != null) {
			updateEnabledState(this.event);
		} else {
			if (container != null) {
				updateEnabledState(container);
			} else {
				super.updateEnabledState();
			}
		}
	}

	/**
	 * Updates 'enabled' state depending on the given address container object.
	 * @param container The address container (maybe null).
	 * @return
	 */
	protected abstract void updateEnabledState(AddressEditContainer container);
	
	/**
	 * Updates 'enabled' state depending on the given address container object.
	 * @param container The address container (maybe null).
	 * @return
	 */
	protected abstract void updateEnabledState(AddressEditSelectionEvent event);

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (event != null) { // use the event acquired previously.
			addressEditActionPerformed(event);	
			event = null; // consume event
		} else {
			if (container != null) {
				addressEditActionPerformed(container);
			} else { // call super class hook
				actionPerformed(arg0);
			}
		}
	}
	

	/**
	 * Redirected action handler for doing actions on a address selection.
	 * @param ev
	 */
	public abstract void addressEditActionPerformed(AddressEditSelectionEvent ev);
	
	/**
	 * Redirected action handler for doing actions on an address container.
	 * @param ev
	 */
	public abstract void addressEditActionPerformed(AddressEditContainer container);
	
	

}
