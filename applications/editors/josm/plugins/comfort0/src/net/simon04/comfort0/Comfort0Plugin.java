package net.simon04.comfort0;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the comfort0 plugin.
 *
 * @author Simon Legner <Simon.Legner@gmail.com>
 */
public class Comfort0Plugin extends Plugin {

    /**
     * Constructs a new {@link Comfort0Plugin}.
     *
     * @param info the plugin information describing the plugin.
     */
    public Comfort0Plugin(PluginInformation info) {
        super(info);
        final MainMenu menu = MainApplication.getMenu();
        MainMenu.add(menu.editMenu, new EditLevel0LAction());

    }
}
