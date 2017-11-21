// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.namemanager;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.namemanager.listeners.NameManagerAction;

/**
 * This is plugin class which constructor is callad at the initialization stage.
 * 
 * @author Rafal Jachowicz, Harman/Becker Automotive Systems (master's thesis)
 * 
 */
public class NameManagerPlugin extends Plugin {
    public NameManagerPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().dataMenu, new NameManagerAction());
    }
}

