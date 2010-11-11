package josmrestartplugin;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class RestartPlugin extends Plugin {
    public RestartPlugin(PluginInformation info) {
        super(info);
        Main.main.menu.fileMenu.insert(new RestartJosmAction(),
                Main.main.menu.fileMenu.getItemCount()-1);
    }
}
