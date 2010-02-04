/**
 * Tracer - plugin for JOSM
 * Jan Bilak
 * This program is free software and licensed under GPL.
 */

package tracer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TracerPlugin extends Plugin {

    public TracerPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.toolsMenu, new TracerAction(Main.map));
    }
}
