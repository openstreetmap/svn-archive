// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

/**
 * Global action objects
 */
public final class AddressActions {

    private AddressActions() {
        // Hide default constructor for utilities classes
    }

    public static SelectAddressesInMapAction getSelectAction() {
        return new SelectAddressesInMapAction();
    }

    public static GuessAddressDataAction getGuessAddressAction() {
        return new GuessAddressDataAction();
    }

    public static ApplyAllGuessesAction getApplyGuessesAction() {
        return new ApplyAllGuessesAction();
    }

    public static RemoveAddressTagsAction getRemoveTagsAction() {
        return new RemoveAddressTagsAction();
    }

    public static AssignAddressToStreetAction getResolveAction() {
        return new AssignAddressToStreetAction();
    }

    public static ConvertToRelationAction getConvertToRelationAction() {
        return new ConvertToRelationAction();
    }

    public static ConvertAllToRelationAction getConvertAllToRelationAction() {
        return new ConvertAllToRelationAction();
    }
}
