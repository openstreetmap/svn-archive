// License: GPL. For details, see LICENSE file.
package CreateGridOfWaysPlugin;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class CreateGridOfWaysPlugin extends Plugin {
    public CreateGridOfWaysPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new CreateGridOfWaysAction());
    }
}
