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
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.fixAddresses.ICommandListener;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.StringUtils;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Base class for all address related action. An action can work as well on all addresses collected by the
 * container or on the active selection.
 * By default, the action is disabled and the updateEnabledState(...) have to be implemented by
 * subclasses. There are also two separate <tt>actionPerformedXX</tt> methods to do the action on
 * container or on selection items.
 * Most actions will work in both cases, so it is recommended to have one single method which
 * accepts a list of addresses or streets and executes the tasks to be done by this action.
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 */

@SuppressWarnings("serial")
public abstract class AbstractAddressEditAction extends JosmAction implements IAddressEditContainerListener, ICommandListener {
	private AddressEditSelectionEvent event;
	protected AddressEditContainer container;
	protected List<Command> commands;
	private String txName;

	/**
	 * @param name
	 * @param icon
	 */
	public AbstractAddressEditAction(String name, String iconName, String tooltip, String toolbar) {
		super(name, iconName, tooltip, null, true, toolbar, true);

		setEnabled(false);
	}

	/**
	 * @param name
	 */
	public AbstractAddressEditAction(String name) {
		this(name, null, "", null);
	}

	/**
	 * Gets the current address container.
	 * @return the container
	 */
	public AddressEditContainer getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(AddressEditContainer container) {
		if (container != null) { // remove old listener first
			container.removeChangedListener(this);
		}
		this.container = container;
		updateEnabledState();
		if (container != null) {
			container.addChangedListener(this);
		}
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
	public void setEvent(AddressEditSelectionEvent event) {
		this.event = event;
		updateEnabledState();
	}

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
			} else {
				throw new RuntimeException("AbstractAddressEditAction has no container or event");
			}
		}
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
	 * Updates 'enabled' state depending on the current selection.
	 * @param container The selection event.
	 * @return
	 */
	protected abstract void updateEnabledState(AddressEditSelectionEvent event);

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


	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener#containerChanged(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	public void containerChanged(AddressEditContainer container) {
		updateEnabledState();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener#entityChanged(org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity)
	 */
	@Override
	public void entityChanged(IOSMEntity node) {
		container.removeProblemsOfSource(node); // clear problems of changed node...
		node.visit(container, container);                   // .. and revisit it.
		updateEnabledState();
	}

	/**
	 * Begins the transaction (command sequence). Must be called by every subclass before
	 * any modification on OSM objects starts.
	 *
	 * @param txName the name of the transaction (e. g. "change address tags").
	 */
	public void beginTransaction(String txName) {
		if (commands != null && commands.size() > 0) {
			throw new RuntimeException("TX has not been closed (missing finishTransaction?)");
		}

		commands = new ArrayList<Command>();
		if (StringUtils.isNullOrEmpty(txName)) {
			throw new RuntimeException("Transaction must have a name");
		}
		this.txName = txName;
	}

	/**
	 * Finishes the transaction and passes the command sequence to the framework.
	 */
	public void finishTransaction() {
		if (commands == null) {
			throw new RuntimeException("No command list available. Did you forget to call beginTransaction?");
		}
		// execute the command
		Main.main.undoRedo.add(new SequenceCommand(txName, commands));
		commands.clear();
		container.invalidate();
	}

	/**
	 * Begins the transaction for a single object.
	 *
	 * @param entity the entity
	 */
	public void beginObjectTransaction(IOSMEntity entity) {
		if (entity != null) {
			entity.addCommandListener(this);
		} else {
			throw new RuntimeException("Entity must not be null");
		}
	}

	/**
	 * Finishes the transaction for a single object.
	 *
	 * @param entity the entity
	 */
	public void finishObjectTransaction(IOSMEntity entity) {
		if (entity != null) {
			entity.removeCommandListener(this);
		} else {
			throw new RuntimeException("Entity must not be null");
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.ICommandListener#commandIssued(org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity, org.openstreetmap.josm.command.Command)
	 */
	@Override
	public void commandIssued(IOSMEntity entity, Command command) {
		if (commands == null) {
			throw new RuntimeException("No command list available. Did you forget to call beginTransaction?");
		}
		commands.add(command);
	}
}
