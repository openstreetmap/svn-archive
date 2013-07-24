package CreateGridOfWaysPlugin;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.gui.MainMenu;

public class CreateGridOfWaysPlugin extends Plugin {
    public CreateGridOfWaysPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.moreToolsMenu, new CreateGridOfWaysAction());
    }
}
