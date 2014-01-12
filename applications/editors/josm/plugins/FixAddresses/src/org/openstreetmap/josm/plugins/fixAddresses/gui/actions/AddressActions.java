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
