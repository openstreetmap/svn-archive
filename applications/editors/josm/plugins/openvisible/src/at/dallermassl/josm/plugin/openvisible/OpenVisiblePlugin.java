/**
 *
 */
package at.dallermassl.josm.plugin.openvisible;

import org.openstreetmap.josm.Main;
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
        MainMenu.add(Main.main.menu.fileMenu, new OpenVisibleAction());
    }

}
