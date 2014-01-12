package org.openstreetmap.josm.plugins.namemanager;

import org.openstreetmap.josm.Main;
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
        MainMenu.add(Main.main.menu.dataMenu, new NameManagerAction());
    }
}

