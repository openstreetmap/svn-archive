package smed;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Smed extends Plugin {

	public Smed(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.toolsMenu, new SmedAction());
    }
}
