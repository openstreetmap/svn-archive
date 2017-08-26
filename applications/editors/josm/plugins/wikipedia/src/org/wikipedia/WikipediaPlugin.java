// License: GPL. For details, see LICENSE file.
package org.wikipedia;

import javax.swing.JMenu;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.wikipedia.actions.FetchWikidataAction;
import org.wikipedia.actions.WikipediaAddNamesAction;
import org.wikipedia.actions.WikipediaCopyTemplate;
import org.wikipedia.gui.WikidataItemSearchDialog;
import org.wikipedia.gui.WikidataTagCellRenderer;
import org.wikipedia.gui.WikipediaToggleDialog;

public class WikipediaPlugin extends Plugin {

    public WikipediaPlugin(PluginInformation info) {
        super(info);
        new WikipediaCopyTemplate();
        JMenu dataMenu = MainApplication.getMenu().dataMenu;
        MainMenu.add(dataMenu, new WikipediaAddNamesAction());
        MainMenu.add(dataMenu, new FetchWikidataAction());
        MainMenu.add(dataMenu, new WikidataItemSearchDialog.Action());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(new WikipediaToggleDialog());
            newFrame.propertiesDialog.addCustomPropertiesCellRenderer(new WikidataTagCellRenderer());
        }
    }
}
