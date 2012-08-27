package smed;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Smed2 extends Plugin {
    public Smed2(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.toolsMenu, new SmedAction());
    }
}
