// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugin.download_along;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DownloadAlong extends Plugin {

    public DownloadAlong(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new DownloadAlongWayAction());
    }
}
