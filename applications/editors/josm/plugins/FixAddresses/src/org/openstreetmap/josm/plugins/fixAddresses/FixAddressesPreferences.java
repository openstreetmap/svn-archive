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
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceSettingFactory;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;

// TODO: Implement
public class FixAddressesPreferences implements PreferenceSetting {
	private boolean selectGuessedObjects = false;

	public static class Factory implements PreferenceSettingFactory {
		public PreferenceSetting createPreferenceSetting() {
			return new FixAddressesPreferences();
		}
	}

	@Override
	public void addGui(PreferenceTabbedPane gui) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean ok() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Checks if option "select guessed objects" is set. If yes, every selection
	 * includes also the objects used for guessing the address tags.
	 * Otherwise only the address itself is selected.
	 *
	 * @return the selectGuessedObjects
	 */
	public boolean isSelectGuessedObjects() {
		return selectGuessedObjects;
	}

	/**
	 * Sets the select guessed objects.
	 *
	 * @param selectGuessedObjects the selectGuessedObjects to set
	 */
	void setSelectGuessedObjects(boolean selectGuessedObjects) {
		this.selectGuessedObjects = selectGuessedObjects;
	}



}
