// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Interface to Darryl Shpak's Lakewalker python module
 *
 * @author Brent Easton
 */
public class LakewalkerPlugin extends Plugin {
    public LakewalkerPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new LakewalkerAction(tr("Lake Walker")));
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new LakewalkerPreferences();
    }

    public static File getLakewalkerCacheDir() {
        return new File(Config.getDirs().getCacheDirectory(true), "lakewalkerwms");
    }
}
