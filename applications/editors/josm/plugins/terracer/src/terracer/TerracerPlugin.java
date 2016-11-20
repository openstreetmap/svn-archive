// License: GPL. For details, see LICENSE file.
package terracer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Plugin interface implementation for Terracer.
 *
 * @author zere - Copyright 2009 CloudMade Ltd
 */
public class TerracerPlugin extends Plugin {
    public TerracerPlugin(PluginInformation info) {
        super(info);

        MainMenu.add(Main.main.menu.moreToolsMenu, new TerracerAction());
        MainMenu.add(Main.main.menu.moreToolsMenu, new ReverseTerraceAction());
    }
}
