// License: GPL. For details, see LICENSE file.
package touchscreenhelper;

import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TouchScreenHelperPlugin extends Plugin {
    public TouchScreenHelperPlugin(PluginInformation info) {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            newFrame.addMapMode(new IconToggleButton(new BrowseAction()));
        }
    }
}
