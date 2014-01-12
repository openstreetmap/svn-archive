// License: GPL. Copyright 2009 by Mike Nice and others
package org.openstreetmap.josm.plugins.AddrInterpolation;


import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class AddrInterpolationPlugin extends Plugin {

    AddrInterpolationAction action = null;

    /**
     * constructor
     */
    public AddrInterpolationPlugin(PluginInformation info) {
        super(info);
        action = new AddrInterpolationAction();
        MainMenu.add(Main.main.menu.dataMenu, action, false, 0);
    }
}
