/**
 *
 */
package at.dallermassl.josm.plugin.openvisible;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * @author cdaller
 *
 */
public class OpenVisiblePlugin extends Plugin {

    public OpenVisiblePlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().gpsMenu, new OpenVisibleAction());
    }

}
