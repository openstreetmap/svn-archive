package ptl;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Plugin extends org.openstreetmap.josm.plugins.Plugin {

    public Plugin(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.dataMenu, new PublicTransportLayer.AddLayerAction());
        MainMenu.add(Main.main.menu.dataMenu, new DistanceBetweenStops());
    }
}

