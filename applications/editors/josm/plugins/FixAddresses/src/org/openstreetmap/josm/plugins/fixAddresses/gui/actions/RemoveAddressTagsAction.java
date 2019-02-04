// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Removes address related tags from the object.
 */
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
