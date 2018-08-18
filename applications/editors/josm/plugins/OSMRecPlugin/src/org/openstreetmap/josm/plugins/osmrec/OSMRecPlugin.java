// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * OSM rec plugin
 * @author imis-nkarag
 */
public class OSMRecPlugin extends Plugin {

    private final MenuExportAction menuExportAction;
    private static MapFrame mapFrame;
    public OSMRecPlugin plugin;

    public OSMRecPlugin(PluginInformation info) { // NO_UCD (unused code)
        super(info);
        menuExportAction = new MenuExportAction();
        MainApplication.getMenu().toolsMenu.add(menuExportAction);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) { // map frame added
            setCurrentMapFrame(newFrame);
            setState(this);
        }
    }

    private void setCurrentMapFrame(MapFrame newFrame) {
        OSMRecPlugin.mapFrame = newFrame;
    }

    public static MapFrame getCurrentMapFrame() {
        return mapFrame;
    }

    private void setState(OSMRecPlugin plugin) {
        this.plugin = plugin;
    }

    public OSMRecPlugin getState() {
        return plugin;
    }

    //    @Override
    //    public PreferenceSetting getPreferenceSetting() {
    //        return new PreferenceEditor();
    //    }
}
