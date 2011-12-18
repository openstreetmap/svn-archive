/*
 *      PrintPlugin.java
 *      
 *      Copyright 2011 Kai Pastor
 *      
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 *      
 *      
 */

package org.openstreetmap.josm.plugins.print;

import java.awt.Toolkit;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * The PrintPlugin class implements the interface JOSM needs to 
 * load extension.
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
     * The default attribution text
     */
    public static final String DEF_ATTRIBUTION = 
      "OSM Map data (c) OpenStreetMap contributors, CC-BY-SA";

    /**
     * Adds the PrintPlugin to the GUI
     *
     * @param info the plugin information describing the plugin.
     */
    public PrintPlugin(PluginInformation info) {
        super(info);

        JMenu fileMenu = Main.main.menu.fileMenu;
        int pos = fileMenu.getItemCount();
        do {
            pos--;
        } while (fileMenu.getItem(pos) != null && pos > 2);

        PrintAction printAction = new PrintAction();
        printMenu = fileMenu.insert(printAction, pos);
        printMenu.setEnabled(false);
        printMenu.setVisible(true);

        KeyStroke ks = printAction.getShortcut().getKeyStroke();
        if (ks != null) {
            printMenu.setAccelerator(ks);
        }

        fileMenu.insertSeparator(pos);

        /* Make this plugin's preferences known */
        Main.pref.putDefault(
          "print.map-scale", Integer.toString(DEF_MAP_SCALE));
        Main.pref.putDefault(
          "print.resolution.dpi", Integer.toString(DEF_RESOLUTION_DPI));
        Main.pref.putDefault(
          "print.attribution", DEF_ATTRIBUTION);
        Main.pref.putDefault(
          "print.preview.enabled", new Boolean(false).toString());

        restorePrefs(); // Recover after crash if neccesseary
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
        printMenu.setEnabled(newFrame != null);
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
        if (! Main.pref.getBoolean("print.saved-prefs", false)) {
            Main.pref.put("print.saved-prefs", true);
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
        if (Main.pref.hasKey(key)) {
            Main.pref.put("print.saved-prefs."+key, Main.pref.get(key));
        }
        Main.pref.putInteger(key, value);
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
        if (Main.pref.hasKey(key)) {
            Main.pref.put("print.saved-prefs."+key, Main.pref.get(key));
        }
        Main.pref.put(key, value);
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
        if (Main.pref.hasKey(key)) {
            Main.pref.put("print.saved-prefs."+key, Main.pref.get(key));
        }
        Main.pref.put(key, value);
    }
    
    /**
     * Undo temporary adjustments to the preferences made by 
     * adjustPrefs().
     */
    public static void restorePrefs() {
        if (Main.pref.getBoolean("print.saved-prefs", false)) {
            restorePref("draw.data.downloaded_area");
            restorePref("mappaint.node.connection-size");
            restorePref("mappaint.node.selected-size");
            restorePref("mappaint.node.tagged-size");
            restorePref("mappaint.node.unselected-size");
            restorePref("mappaint.node.virtual-size");
            Main.pref.put("print.saved-prefs", false);
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
        Main.pref.put(key, Main.pref.get(savedKey));
        Main.pref.put(savedKey, null);
    }
    
}

