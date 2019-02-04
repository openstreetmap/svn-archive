// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceSettingFactory;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Plugin preferences.
 */
public final class FixAddressesPreferences extends DefaultTabPreferenceSetting {
    private static final String FIX_ADDRESSES_IGNORE_POST_CODE_KEY = "fixAddresses.ignorePostCode";
    private static final String FIX_ADDRESSES_SELECT_GUESSED_OBJECTS_KEY = "fixAddresses.selectGuessedObjects";

    private JCheckBox cbSelectGuessedObjects = new JCheckBox(tr("Include objects used for guesses"));
    private JCheckBox cbIgnorePostCode = new JCheckBox();

    /**
     * Internal factory class. Call <code>FixAddressesPreferences.Factory().createPreferenceSetting()</code> to
     * create the preference setting instance.
     */
    public static class Factory implements PreferenceSettingFactory {
        @Override
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
        setSelectGuessedObjects(Config.getPref().getBoolean(FIX_ADDRESSES_SELECT_GUESSED_OBJECTS_KEY, false));
        setIgnorePostCode(Config.getPref().getBoolean(FIX_ADDRESSES_IGNORE_POST_CODE_KEY, false));
    }

    /**
     * Save the preference settings.
     */
    private void saveToPrefs() {
        Config.getPref().putBoolean(FIX_ADDRESSES_SELECT_GUESSED_OBJECTS_KEY, isSelectGuessedObjects());
        Config.getPref().putBoolean(FIX_ADDRESSES_IGNORE_POST_CODE_KEY, isIgnorePostCode());
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        // Import settings
        ButtonGroup fixAddrOptions = new ButtonGroup();
        fixAddrOptions.add(cbSelectGuessedObjects);
        fixAddrOptions.add(cbIgnorePostCode);
    }

    @Override
    public boolean ok() {
        saveToPrefs();
        loadFromPrefs();
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
     * @return {@code true} if invalid post codes should be ignored
     */
    public boolean isIgnorePostCode() {
        return cbIgnorePostCode.isSelected();
    }

    public void setIgnorePostCode(boolean ignorePostCode) {
        cbIgnorePostCode.setSelected(ignorePostCode);
    }
}
