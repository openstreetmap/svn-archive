// License: GPL. See LICENSE file for details.
package org.wikipedia;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class WikipediaPlugin extends Plugin {

    public WikipediaPlugin(PluginInformation info) {
        super(info);
        new WikipediaCopyTemplate();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(new WikipediaToggleDialog());
        }
    }
}
