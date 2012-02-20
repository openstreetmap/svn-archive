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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.GuessAddressRunnable;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.IProgressMonitorFinishedListener;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Guesses address tags by picking the closest street node with a name. The same is done
 * with city, post code, state,... However, I strongly encourage you to check the result.
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 */

@SuppressWarnings("serial")
public class GuessAddressDataAction extends AbstractAddressEditAction implements IProgressMonitorFinishedListener {

	/**
	 * Instantiates a new "guess address data" action.
	 */
	public GuessAddressDataAction() {
		super(tr("Guess"), "guessstreets_24", tr("Tries to guess address data by picking the name of the closest object with according tag."),
			"fixaddresses/guessaddressdata");
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void updateEnabledState(AddressEditSelectionEvent ev) {
		setEnabled(ev != null && ev.hasAddresses());
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(container != null && container.getNumberOfInvalidAddresses() > 0);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		if (container == null || container.getNumberOfInvalidAddresses() == 0) return;

		internalGuessAddresses(container.getAllAddressesToFix());
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		if (ev == null || !ev.hasAddresses()) return;

		// guess tags for selected addresses only
		internalGuessAddresses(ev.getSelectedIncompleteAddresses());
		internalGuessAddresses(ev.getSelectedUnresolvedAddresses());
	}

	/**
	 * Internal method to start several threads guessing tag values for the given list of addresses.
	 * @param addrNodes
	 */
	private void internalGuessAddresses(List<OSMAddress> nodes) {
		if (nodes == null) return;

		// Launch address guessing thread
		GuessAddressRunnable aft = new GuessAddressRunnable(nodes, tr("Guessing address values"));
		aft.addFinishListener(this);
		Main.worker.submit(aft);
	}

	@Override
	public void finished() {
		if (container != null) {
			container.invalidate();
		}
	}
}
