// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * This is the main class for the PointInfo plugin.
 * @author Marián Kyral
 */
public class PointInfoPlugin extends Plugin {

    public PointInfoPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.moreToolsMenu, new PointInfoAction(Main.map));
    }
}
