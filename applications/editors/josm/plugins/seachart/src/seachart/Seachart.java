// License: GPL. For details, see LICENSE file.
package seachart;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * @author Malcolm Herring
 */
public class Seachart extends Plugin {

    public Seachart(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.imageryMenu, new SeachartAction());
    }
}
