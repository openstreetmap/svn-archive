// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Create relation between street and related addresses for ALL streets in the current layer.
 */
@SuppressWarnings("serial")
public class ConvertAllToRelationAction extends ConvertToRelationAction {
    public ConvertAllToRelationAction() {
        super(tr("Convert ALL streets."), "convert2rel_24",
            tr("Create relation between street and related addresses for ALL streets in the current layer."),
            "fixaddresses/convertalltorelation");
    }

    @Override
    public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
        // nothing to do
    }

    @Override
    public void addressEditActionPerformed(AddressEditContainer container) {
        if (container != null) {
            for (OSMStreet street : container.getStreetList()) {
                createRelationForStreet(street);
            }
        }
    }

    @Override
    protected void updateEnabledState(AddressEditContainer container) {
        setEnabled(hasStreetsToConvert());
    }

    @Override
    protected void updateEnabledState(AddressEditSelectionEvent event) {
        setEnabled(hasStreetsToConvert());
    }

    /**
     * Checks for streets to convert to a relation.
     *
     * @return true, if successful
     */
    private boolean hasStreetsToConvert() {
        if (container != null) {
            for (OSMStreet street : container.getStreetList()) {
                if (street.hasAddresses() && !street.hasAssociatedStreetRelation()) {
                    return true;
                }
            }
        }
        return false;
    }

}
