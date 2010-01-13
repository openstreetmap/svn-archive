package MichiganLeft;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class MichiganLeft extends Plugin {
    JMenuItem MichiganLeft;

    public MichiganLeft(PluginInformation info) {
    	super(info);
        MichiganLeft = MainMenu.add(Main.main.menu.toolsMenu, new MichiganLeftAction());

    }
}
