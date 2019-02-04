// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;
import org.openstreetmap.josm.plugins.fixAddresses.TagConstants;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Create relation between street and related addresses.
 */
@SuppressWarnings("serial")
public class ConvertToRelationAction extends AbstractAddressEditAction {

    public ConvertToRelationAction() {
        super(tr("Convert to relation."), "convert2rel_24",
            tr("Create relation between street and related addresses."),
            "fixaddresses/converttorelation");
    }

    /**
     * Instantiates a new convert to relation action.
     *
     * @param name the name of the action
     * @param iconName the icon name
     * @param tooltip the tool tip to show on hover
     * @param toolbar identifier for the toolbar preferences
     */
    public ConvertToRelationAction(String name, String iconName, String tooltip, String toolbar) {
        super(name, iconName, tooltip, toolbar);
    }

    @Override
    public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
        OSMStreet streetNode = ev.getSelectedStreet();

        if (streetNode != null) {
            createRelationForStreet(streetNode);
        }
    }

    /**
     * Creates the 'associatedStreet' relation for a given street by adding all addresses which
     * matches the name of the street.
     *
     * @param streetNode the street node
     */
    protected void createRelationForStreet(OSMStreet streetNode) {
        if (streetNode == null || !streetNode.hasAddresses()) return;

        beginTransaction(tr("Create address relation for ") + " '" + streetNode.getName() + "'");
        // Create the relation
        Relation r = new Relation();
        commands.add(new AddCommand(getLayerManager().getEditDataSet(), r));
        commands.add(new ChangePropertyCommand(r, TagConstants.NAME_TAG, streetNode.getName()));
        commands.add(new ChangePropertyCommand(r, TagConstants.RELATION_TYPE, TagConstants.ASSOCIATEDSTREET_RELATION_TYPE));
        // add street with role 'street'
        r.addMember(new RelationMember(TagConstants.STREET_RELATION_ROLE, streetNode.getOsmObject()));

        // add address members
        for (OSMAddress addrNode : streetNode.getAddresses()) {
            beginObjectTransaction(addrNode);
            r.addMember(new RelationMember(TagConstants.HOUSE_RELATION_ROLE, addrNode.getOsmObject()));
            addrNode.setStreetName(null); // remove street name
            finishObjectTransaction(addrNode);
        }
        finishTransaction();
    }

    @Override
    public void addressEditActionPerformed(AddressEditContainer container) {
        // Nothing to do (yet).
    }

    @Override
    protected void updateEnabledState(AddressEditContainer container) {
        setEnabled(false);
    }

    @Override
    protected void updateEnabledState(AddressEditSelectionEvent event) {
        if (event == null) return;

        OSMStreet street = event.getSelectedStreet();
        setEnabled(street != null && street.hasAddresses() && !street.hasAssociatedStreetRelation());
    }
}
