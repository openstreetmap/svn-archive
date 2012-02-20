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

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

@SuppressWarnings("serial")
public class RemoveAddressTagsAction extends AbstractAddressEditAction {

	public RemoveAddressTagsAction() {
		super(tr("Remove"), "removeaddrtags_24", tr("Removes address related tags from the object."),
			"fixaddresses/removeaddresstags");
	}

	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		beginTransaction(tr("Remove address tags"));
		if (ev.hasUnresolvedAddresses()) {
			for (OSMAddress aNode : ev.getSelectedUnresolvedAddresses()) {
				beginObjectTransaction(aNode);
				aNode.removeAllAddressTags();
				finishObjectTransaction(aNode);
			}
		}

		if (ev.hasIncompleteAddresses()) {
			for (OSMAddress aNode : ev.getSelectedIncompleteAddresses()) {
				beginObjectTransaction(aNode);
				aNode.removeAllAddressTags();
				finishObjectTransaction(aNode);
			}
		}
		finishTransaction();
	}

	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		// do nothing
	}

	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(false);
	}

	@Override
	protected void updateEnabledState(AddressEditSelectionEvent event) {
		if (event == null) {
			setEnabled(false);
		}

		setEnabled(event.hasAddresses());
	}

}
