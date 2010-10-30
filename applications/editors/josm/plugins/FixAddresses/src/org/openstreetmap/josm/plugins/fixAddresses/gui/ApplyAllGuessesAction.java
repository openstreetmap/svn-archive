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

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.AddressNode;

@SuppressWarnings("serial")
public class ApplyAllGuessesAction extends AbstractAddressEditAction {

	public ApplyAllGuessesAction() {
		//super(tr("Apply all guesses"), "applyguesses_24", "Turns all guesses into the corresponding tag values.");
		super(tr("Apply all guesses"), "applyguesses_24", "Turns all guesses into the corresponding tag values.");
	}

	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		if (ev == null || ev.getSelectedUnresolvedAddresses() == null) return;
		// fix SELECTED items only
		List<AddressNode> addrToFix = ev.getSelectedUnresolvedAddresses();
		applyGuesses(addrToFix);
	}

	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(container != null && container.getNumberOfGuesses() > 0);
	}

	private void applyGuesses(List<AddressNode> addrToFix) {
		List<AddressNode> addrToFixShadow = new ArrayList<AddressNode>(addrToFix);
		for (AddressNode aNode : addrToFixShadow) {
			aNode.applyAllGuesses();
		}
	}

	@Override
	protected void updateEnabledState(AddressEditSelectionEvent event) {
		// do nothing here
	}

	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		if (container == null || container.getUnresolvedAddresses() == null) return;
		
		List<AddressNode> addrToFix = container.getUnresolvedAddresses();
		applyGuesses(addrToFix);		
	}
}
