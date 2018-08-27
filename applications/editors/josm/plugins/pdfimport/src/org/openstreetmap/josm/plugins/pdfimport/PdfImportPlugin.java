// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * A plugin to import a PDF file.
 */
public class PdfImportPlugin extends Plugin {

    public PdfImportPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().imagerySubMenu, new PdfImportAction());
        new Preferences(getPluginInformation().name);
    }

    public static class pdfimportPrefs implements SubPreferenceSetting {
        @Override
        public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
            return null;
        }

        @Override
        public void addGui(PreferenceTabbedPane gui) {
            return;
        }

        @Override
        public boolean ok() {
            return false;
        }

        @Override
        public boolean isExpert() {
            return false;
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        /*
         * TODO: implement it
         */
        return new pdfimportPrefs();
        }

}
