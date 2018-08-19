// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.terracer;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Plugin interface implementation for Terracer.
 *
 * @author zere - Copyright 2009 CloudMade Ltd
 */
public class TerracerPlugin extends Plugin {
    public TerracerPlugin(PluginInformation info) {
        super(info);

        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new TerracerAction());
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new ReverseTerraceAction());
    }
}
