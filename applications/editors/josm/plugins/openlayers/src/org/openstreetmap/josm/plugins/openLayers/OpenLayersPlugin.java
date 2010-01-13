package org.openstreetmap.josm.plugins.openLayers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the OpenLayers plugin.
 *
 * @author Francisco R. Santos <frsantos@gmail.com>
 *
 */
public class OpenLayersPlugin extends Plugin {

    static OpenLayersLayer layer;
    static JMenu menu;
    static String pluginDir;

    public OpenLayersPlugin(PluginInformation info) {
    	super(info);
        pluginDir = getPluginDir();
        try {
            copy("/resources/yahoo.html", "yahoo.html");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StorageManager.initStorage( pluginDir );
        refreshMenu();
    }

    public static void refreshMenu() {
        JMenuBar menuBar = Main.main.menu;
        if (menu == null) {
            menu = new JMenu(tr("OpenLayers"));
            menuBar.add(menu, 5);
        } else {
            menu.removeAll();
        }

        menu.add(new JMenuItem(new ShowOpenLayersAction("Yahoo")));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openstreetmap.josm.plugins.Plugin#getPreferenceSetting()
     */
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return null;
    }
}
