package MichiganLeft;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;

public class MichiganLeft extends Plugin {
    JMenuItem MichiganLeft;

    public MichiganLeft() {
        MichiganLeft = MainMenu.add(Main.main.menu.toolsMenu, new MichiganLeftAction());

    }
}
