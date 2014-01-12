package org.openstreetmap.josm.plugins.walkingpapers;

import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the walking papers plugin.
 *
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class WalkingPapersPlugin extends Plugin
{
    static JMenu walkingPapersMenu;

    public WalkingPapersPlugin(PluginInformation info)
    {
        super(info);
        walkingPapersMenu = Main.main.menu.imagerySubMenu;
         // ht("/Plugin/WalkingPapers"));
        MainMenu.add(walkingPapersMenu, new WalkingPapersAddLayerAction(), false, 0);
    }
}
