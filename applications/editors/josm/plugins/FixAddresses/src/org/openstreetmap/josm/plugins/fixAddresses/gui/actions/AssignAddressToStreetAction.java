// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Assigns one or more selected addresses to a street, i. e. the name of the street is
 * used as value for the addr:street tag.
 * @author Oliver Wieland &lt;oliver.wieland@online.de>
 */
public class AssignAddressToStreetAction extends AbstractAddressEditAction {

    /**
     * Instantiates a new "assign address to street" action.
     */
    public AssignAddressToStreetAction() {
        super(tr("Assign address to street"), "assignstreet_24",
            tr("Assign the selected address(es) to the selected street."),
            "fixaddresses/assignaddresstostreet");
    }

    /**
     *
     */
    private static final long serialVersionUID = -6180491357232121384L;

    @Override
    public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
        OSMStreet streetNode = ev.getSelectedStreet();

        if (streetNode != null && ev.getSelectedUnresolvedAddresses() != null) {
            beginTransaction(tr("Set street name") + " '" + streetNode.getName() + "'");
            for (OSMAddress addrNode : ev.getSelectedUnresolvedAddresses()) {
                beginObjectTransaction(addrNode);
                addrNode.assignStreet(streetNode);
                finishObjectTransaction(addrNode);
            }
            finishTransaction();
        }

    }

    @Override
    public void updateEnabledState(AddressEditSelectionEvent ev) {
        setEnabled(ev.getSelectedStreet() != null && ev.hasUnresolvedAddresses());
    }

    @Override
    public void updateEnabledState(AddressEditContainer container) {
        // we only accept a selection here
        setEnabled(false);
    }

    @Override
    public void addressEditActionPerformed(AddressEditContainer container) {
        // we only accept a selection: nothing to do here
    }
}
