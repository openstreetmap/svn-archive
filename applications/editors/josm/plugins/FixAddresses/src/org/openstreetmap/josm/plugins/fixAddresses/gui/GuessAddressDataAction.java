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

import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.AddressFinderThread;
import org.openstreetmap.josm.plugins.fixAddresses.AddressNode;

/**
 * Guesses address tags by picking the closest street node with a name. The same is done (some day)
 * with city, post code, state,... However, I strongly encourage you to check the result.
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

@SuppressWarnings("serial")
public class GuessAddressDataAction extends AbstractAddressEditAction {
	private static final int THREAD_COUNT = 5;
	private AddressFinderThread[] threads = new AddressFinderThread[THREAD_COUNT];

	public GuessAddressDataAction() {
		super(tr("Guess address data"), "guessstreets_24", "Tries to guess the street name by picking the name of the closest way.");
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void updateEnabledState(AddressEditSelectionEvent ev) {
		setEnabled(ev != null && ev.getUnresolvedAddressTable() != null);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(container != null && container.getNumberOfIncompleteAddresses() > 0);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		if (container == null) return;
		if (container.getUnresolvedAddresses() == null) return;
				
		internalGuessAddresses(container.getIncompleteAddresses());
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		if (ev == null || ev.getSelectedUnresolvedAddresses() == null) return;
		
		// guess tags for selected addresses only
		internalGuessAddresses(ev.getSelectedUnresolvedAddresses());		
	}
	
	/**
	 * Internal method to start several threads guessing tag values for the given list of addresses.
	 * @param addrNodes
	 */
	private void internalGuessAddresses(List<AddressNode> nodes) {
		Main.worker.submit(new AddressFinderThread(container, tr("Guess street names")));		
	}
}
