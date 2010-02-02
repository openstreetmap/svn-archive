/**
 * Tracer - plugin for JOSM
 * Jan Bilak
 * This program is free software and licensed under GPL.
 */

package tracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TracerPlugin extends Plugin {

    public TracerPlugin() {
        MainMenu.add(Main.main.menu.toolsMenu, new TracerAction(Main.main.map));
    }
}
