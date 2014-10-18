// License: GPL. For details, see LICENSE file.
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
            List<OsmPrimitive> osms = new ArrayList<>();

            for (OSMAddress aNode : addressEditContainer.getIncompleteAddresses()) {
                osms.add(aNode.getOsmObject());
            }
            getCurrentDataSet().setSelected(osms);
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getCurrentDataSet() != null);
    }
}
