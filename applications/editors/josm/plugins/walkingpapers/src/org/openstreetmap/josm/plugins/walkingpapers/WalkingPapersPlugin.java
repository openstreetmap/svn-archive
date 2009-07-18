package org.openstreetmap.josm.plugins.walkingpapers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * Main class for the walking papers plugin.
 *
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class WalkingPapersPlugin extends Plugin
{
	static JMenu walkingPapersMenu;
	
    public WalkingPapersPlugin()
    {
        MainMenu menu = Main.main.menu;
        // Note to translators. Please do not translate "Walking Papers", because it is 
        // the name of the project (www.walking-papers.org) and should retain this name
        // in every language until and unless the project itself is i18nised.
        walkingPapersMenu = menu.addMenu("Walking Papers", KeyEvent.VK_K, menu.defaultMenuPos);
        walkingPapersMenu.add(new JMenuItem(new WalkingPapersAddLayerAction()));
    }

}
