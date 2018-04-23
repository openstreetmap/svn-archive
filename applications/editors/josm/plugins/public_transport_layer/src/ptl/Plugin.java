// License: GPL. For details, see LICENSE file.
package ptl;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Public transport layer plugin.
 */
public class Plugin extends org.openstreetmap.josm.plugins.Plugin {

    public Plugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().dataMenu, new PublicTransportLayer.AddLayerAction());
        MainMenu.add(MainApplication.getMenu().dataMenu, new DistanceBetweenStops());
    }
}
