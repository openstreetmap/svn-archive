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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;
import org.openstreetmap.josm.plugins.addressEdit.AddressFinderThread;
import org.openstreetmap.josm.plugins.addressEdit.AddressNode;

@SuppressWarnings("serial")
public class GuessAddressDataAction extends AbstractAddressEditAction {
	private static final int THREAD_COUNT = 5;
	private AddressFinderThread[] threads = new AddressFinderThread[THREAD_COUNT];

	public GuessAddressDataAction() {
		super(tr("Guess address data"));
	}

	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (container == null) return;
		if (container.getUnresolvedAddresses() == null) return;
		
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new AddressFinderThread();
		}
						
		List<AddressNode> addrNodes = new ArrayList<AddressNode>();		
		addrNodes.addAll(container.getIncompleteAddresses());
		for (AddressNode aNode : addrNodes) {
			if (aNode.hasStreetName()) continue;
			
			while(!scheduleNode(aNode)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
		container.containerChanged(container);
	}

	private boolean scheduleNode(AddressNode aNode) {
		for (int i = 0; i < threads.length; i++) {
			if (!threads[i].isRunning()) {
				threads[i].setAddressNode(aNode);
				threads[i].run();
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.gui.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void updateEnabledState(AddressEditSelectionEvent ev) {
		// do nothing here
	}

	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(container != null && container.getNumberOfIncompleteAddresses() > 0);
	}

	
}
