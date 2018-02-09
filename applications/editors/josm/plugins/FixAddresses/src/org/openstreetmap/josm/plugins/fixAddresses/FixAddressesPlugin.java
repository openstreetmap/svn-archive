// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.fixAddresses.gui.IncompleteAddressesDialog;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.SelectIncompleteAddressesAction;

/**
 * The Class FixAddressesPlugin is the main entry point for the plugin.
 */
public class FixAddressesPlugin extends Plugin {
    private static FixAddressesPreferences preferences;

    /**
     * Constructor for the AddressEdit plugin. Called by JOSM when loading the plugin.
     * @param info Context information of the plugin.
     */
    public FixAddressesPlugin(PluginInformation info) {
        super(info);

        // Create actions...
        FixUnresolvedStreetsAction action = new FixUnresolvedStreetsAction();
        SelectIncompleteAddressesAction incAddrAction = new SelectIncompleteAddressesAction();

        // ... and add them to JOSM menus
        MainMenu.add(MainApplication.getMenu().dataMenu, action, false);
        MainMenu.add(MainApplication.getMenu().selectionMenu, incAddrAction);

        // create preferences instance
        preferences = (FixAddressesPreferences) new FixAddressesPreferences.Factory().createPreferenceSetting();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            FixAddressesMapMode faMode = new FixAddressesMapMode();
            IconToggleButton faModeButton = new IconToggleButton(faMode);
            faModeButton.setVisible(true);
            newFrame.addToggleDialog(new IncompleteAddressesDialog());
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return getPreferences();
    }

    /**
     * Gets the preferences instance for this plugin.
     *
     * @return the preferences
     */
    public static FixAddressesPreferences getPreferences() {
        return preferences;
    }
}
