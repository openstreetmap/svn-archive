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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;

@SuppressWarnings("serial")
public class SelectIncompleteAddressesAction extends JosmAction {


	private AddressEditContainer addressEditContainer;

	public SelectIncompleteAddressesAction() {
		super(tr("Select incomplete addresses"), "select_invaddr_24",
				tr("Selects all addresses with incomplete data."), null, false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		addressEditContainer = new AddressEditContainer();
		addressEditContainer.invalidate();

		if (addressEditContainer.getIncompleteAddresses() != null) {
			List<OsmPrimitive> osms = new ArrayList<OsmPrimitive>();

			for (OSMAddress aNode : addressEditContainer.getIncompleteAddresses()) {
				osms.add(aNode.getOsmObject());
			}
			getCurrentDataSet().setSelected(osms);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.actions.JosmAction#updateEnabledState()
	 */
	@Override
	protected void updateEnabledState() {
		setEnabled(getCurrentDataSet() != null);
	}
}
