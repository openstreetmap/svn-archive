// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.saudinationaladdress;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * This plugin uses the Saudi National Address database to get any building address
 * in the Kingdom.
 */
public class SaudiNationalAddressPlugin extends Plugin {

    /**
     * constructor
     *
     * @param info plugin info
     */
    public SaudiNationalAddressPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().toolsMenu, new SaudiNationalAddressAction());
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new SaudiNationalAddressPreference();
    }
}
