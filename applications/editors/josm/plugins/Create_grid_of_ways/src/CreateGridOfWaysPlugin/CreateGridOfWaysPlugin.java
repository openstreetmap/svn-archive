package CreateGridOfWaysPlugin;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MainMenu;

public class CreateGridOfWaysPlugin extends Plugin {
    public CreateGridOfWaysPlugin() {
        MainMenu.add(Main.main.menu.toolsMenu, new CreateGridOfWaysAction());
    }
}
