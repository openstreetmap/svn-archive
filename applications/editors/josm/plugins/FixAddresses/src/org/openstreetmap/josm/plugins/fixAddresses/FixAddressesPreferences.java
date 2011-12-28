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

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceSettingFactory;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import static org.openstreetmap.josm.tools.I18n.tr;

public class FixAddressesPreferences implements PreferenceSetting {
	private static final String FIX_ADDRESSES_IGNORE_POST_CODE_KEY = "fixAddresses.ignorePostCode";
	private static final String FIX_ADDRESSES_SELECT_GUESSED_OBJECTS_KEY = "fixAddresses.selectGuessedObjects";
	
	private JCheckBox cbSelectGuessedObjects = new JCheckBox(tr("Include objects used for guesses"));
	private JCheckBox cbIgnorePostCode = new JCheckBox();

	/**
	 * Internal factory class. Call <code>FixAddressesPreferences.Factory().createPreferenceSetting()</code> to
	 * create the preference setting instance. 
	 */
	public static class Factory implements PreferenceSettingFactory {
		public PreferenceSetting createPreferenceSetting() {
			return new FixAddressesPreferences();
		}
	}
	
	/**
	 * Internal constructor.
	 */
	private FixAddressesPreferences() {
		loadFromPrefs();
	}
	
	/**
	 * Loads the (initial) preference settings.
	 */
	private void loadFromPrefs() {
		@SuppressWarnings("unused")
		boolean test = Main.pref.getBoolean(FIX_ADDRESSES_IGNORE_POST_CODE_KEY, false);
		setSelectGuessedObjects(Main.pref.getBoolean(FIX_ADDRESSES_SELECT_GUESSED_OBJECTS_KEY, false));
		setIgnorePostCode(Main.pref.getBoolean(FIX_ADDRESSES_IGNORE_POST_CODE_KEY, false));
	}
	
	/**
	 * Save the preference settings.
	 */
	private void saveToPrefs() {
		Main.pref.put(FIX_ADDRESSES_SELECT_GUESSED_OBJECTS_KEY, isSelectGuessedObjects());
		Main.pref.put(FIX_ADDRESSES_IGNORE_POST_CODE_KEY, isIgnorePostCode());
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.preferences.PreferenceSetting#addGui(org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane)
	 */
	@Override
	public void addGui(PreferenceTabbedPane gui) {
		// Import settings
        ButtonGroup fixAddrOptions = new ButtonGroup();
        fixAddrOptions.add(cbSelectGuessedObjects);
        fixAddrOptions.add(cbIgnorePostCode);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.preferences.PreferenceSetting#ok()
	 */
	@Override
	public boolean ok() {
		saveToPrefs();
		return true;
	}

	/**
	 * Checks if option "select guessed objects" is set. If yes, every selection
	 * includes also the objects used for guessing the address tags.
	 * Otherwise only the address itself is selected.
	 *
	 * @return the selectGuessedObjects
	 */
	public boolean isSelectGuessedObjects() {
		return cbSelectGuessedObjects.isSelected();
	}

	/**
	 * Sets the select guessed objects.
	 *
	 * @param selectGuessedObjects the selectGuessedObjects to set
	 */
	void setSelectGuessedObjects(boolean selectGuessedObjects) {
		cbSelectGuessedObjects.setSelected(selectGuessedObjects);
	}

	/**
	 * Checks if invalid post codes should be ignored. If yes, post codes are neither
	 * checked for existence nor for correctness.
	 * @return
	 */
	public boolean isIgnorePostCode() {
		return cbIgnorePostCode.isSelected();
	}

	public void setIgnorePostCode(boolean ignorePostCode) {
		cbIgnorePostCode.setSelected(ignorePostCode);
	}

}
