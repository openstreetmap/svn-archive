// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

public final class AddressActions {
    /* Global action objects */
    private static SelectAddressesInMapAction selectAction = new SelectAddressesInMapAction();
    private static GuessAddressDataAction guessDataAction = new GuessAddressDataAction();
    private static ApplyAllGuessesAction applyGuessesAction = new ApplyAllGuessesAction();
    private static RemoveAddressTagsAction removeTagsAction = new RemoveAddressTagsAction();
    private static AssignAddressToStreetAction resolveAction = new AssignAddressToStreetAction();
    private static ConvertToRelationAction convertToRelationAction = new ConvertToRelationAction();
    private static ConvertAllToRelationAction convertAllToRelationAction = new ConvertAllToRelationAction();
    
    public static SelectAddressesInMapAction getSelectAction() {
        return selectAction;
    }
    public static GuessAddressDataAction getGuessAddressAction() {
        return guessDataAction;
    }
    public static ApplyAllGuessesAction getApplyGuessesAction() {
        return applyGuessesAction;
    }
    public static RemoveAddressTagsAction getRemoveTagsAction() {
        return removeTagsAction;
    }
    public static AssignAddressToStreetAction getResolveAction() {
        return resolveAction;
    }
    public static ConvertToRelationAction getConvertToRelationAction() {
        return convertToRelationAction;
    }
    public static ConvertAllToRelationAction getConvertAllToRelationAction() {
        return convertAllToRelationAction;
    }
}
