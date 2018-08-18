// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.print;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * The PrintPlugin class implements the interface JOSM needs to
 * load extension.
 * @author Kai Pastor
 */
public class PrintPlugin extends Plugin {

    /**
     * The menu item for the print action
     */
    private JMenuItem printMenu;

    /**
     * The default map scale
     */
    public static final int DEF_MAP_SCALE = 25000;

    /**
     * The default resolution
     */
    public static final int DEF_RESOLUTION_DPI = 100;

    /**
     * Adds the PrintPlugin to the GUI
     *
     * @param info the plugin information describing the plugin.
     */
    public PrintPlugin(PluginInformation info) {
        super(info);

        JMenu fileMenu = MainApplication.getMenu().fileMenu;
        int pos = fileMenu.getItemCount();
        do {
            pos--;
        } while (fileMenu != null && pos > 2 && fileMenu.getItem(pos) != null);

        if (pos > 0) {
            PrintAction printAction = new PrintAction();
            printMenu = fileMenu.insert(printAction, pos);
            printMenu.setEnabled(false);
            printMenu.setVisible(true);

            KeyStroke ks = printAction.getShortcut().getKeyStroke();
            if (ks != null) {
                printMenu.setAccelerator(ks);
            }

            fileMenu.insertSeparator(pos);
        }

        restorePrefs(); // Recover after crash if necessary
    }

    /**
     * Enables/disables the print action in the GUI
     * when a MapFrame gets shown or removed.
     *
     * @param oldFrame ignored.
     *
     * @param newFrame the new mapFrame.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (printMenu != null) {
            printMenu.setEnabled(newFrame != null);
        }
    }

    /**
     * Temporary adjust the preferences for map printing
     *
     * This will adjust some preferences such that elements which are
     * not useful on paper will not be printed. This includes the
     * following markup: downloaded area; node markers for connection,
     * selected, unselected, tagged state.
     *
     * Changes will be undone by restorePrefs().
     */
    public static void adjustPrefs() {
        if (!Config.getPref().getBoolean("print.saved-prefs", false)) {
            Config.getPref().putBoolean("print.saved-prefs", true);
            adjustPref("draw.data.downloaded_area", false);
            adjustPref("mappaint.node.connection-size", 0);
            adjustPref("mappaint.node.selected-size", 0);
            adjustPref("mappaint.node.tagged-size", 0);
            adjustPref("mappaint.node.unselected-size", 0);
            adjustPref("mappaint.node.virtual-size", 0);
        }
    }

    /**
     * Adjust a single preference.
     *
     * Saves the existing value for later restorePref.
     *
     * @param key the preference key
     * @param the temporary new int value
     */
    protected static void adjustPref(String key, int value) {
        if (!Config.getPref().get(key).isEmpty()) {
            Config.getPref().put("print.saved-prefs."+key, Config.getPref().get(key));
        }
        Config.getPref().putInt(key, value);
    }

    /**
     * Adjust a single preference.
     *
     * Saves the existing value for later restorePref.
     *
     * @param key the preference key
     * @param the temporary new boolean value
     */
    protected static void adjustPref(String key, boolean value) {
        if (!Config.getPref().get(key).isEmpty()) {
            Config.getPref().put("print.saved-prefs."+key, Config.getPref().get(key));
        }
        Config.getPref().putBoolean(key, value);
    }

    /**
     * Adjust a single preference.
     *
     * Saves the existing value for later restorePref.
     *
     * @param key the preference key
     * @param the temporary new String value
     */
    protected static void adjustPref(String key, String value) {
        if (!Config.getPref().get(key).isEmpty()) {
            Config.getPref().put("print.saved-prefs."+key, Config.getPref().get(key));
        }
        Config.getPref().put(key, value);
    }

    /**
     * Undo temporary adjustments to the preferences made by adjustPrefs().
     */
    public static void restorePrefs() {
        if (Config.getPref().getBoolean("print.saved-prefs", false)) {
            restorePref("draw.data.downloaded_area");
            restorePref("mappaint.node.connection-size");
            restorePref("mappaint.node.selected-size");
            restorePref("mappaint.node.tagged-size");
            restorePref("mappaint.node.unselected-size");
            restorePref("mappaint.node.virtual-size");
            Config.getPref().putBoolean("print.saved-prefs", false);
            //Main.main.map.mapView.repaint();
        }
    }

    /**
     * Restore a single preference previously saved by adjustPref()
     *
     * @param key the preference key to be restored
     */
    protected static void restorePref(String key) {
        String savedKey = "print.saved-prefs."+key;
        Config.getPref().put(key, Config.getPref().get(savedKey));
        Config.getPref().put(savedKey, null);
    }
}
