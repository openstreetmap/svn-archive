// License: GPL. For details, see LICENSE file.
package org.wikipedia;

import javax.swing.JMenu;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.wikipedia.actions.FetchWikidataAction;
import org.wikipedia.actions.WikipediaAddNamesAction;
import org.wikipedia.actions.WikipediaCopyTemplate;
import org.wikipedia.gui.*;

public class WikipediaPlugin extends Plugin {

    private PreferenceSetting preferences;

    public WikipediaPlugin(PluginInformation info) {
        super(info);
        new WikipediaCopyTemplate();
        JMenu dataMenu = MainApplication.getMenu().dataMenu;
        MainMenu.add(dataMenu, new WikipediaAddNamesAction());
        MainMenu.add(dataMenu, new FetchWikidataAction());
        MainMenu.add(dataMenu, new WikidataItemSearchDialog.Action());

        DownloadDialog.getInstance().addDownloadSource(new WikosmDownloadSource());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(new WikipediaToggleDialog());
            newFrame.propertiesDialog.addCustomPropertiesCellRenderer(new WikidataTagCellRenderer());
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        if (preferences == null) {
            preferences = (new WikosmServerPreference.Factory()).createPreferenceSetting();
        }
        return preferences;
    }
}
