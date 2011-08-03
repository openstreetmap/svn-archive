/*
 * This file is part of ImproveWayAccuracy plugin for JOSM.
 * http://wiki.openstreetmap.org/wiki/JOSM/Plugins/ImproveWayAccuracy
 *
 * Licence: GPL v2 or later
 * Author:  Alexei Kasatkin, 2011
 * Ideas: Kotelnikov, Michael Barabanov (ticket #3840)
 */

package org.openstreetmap.josm.plugins.fastdraw;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
public class FastDrawingPlugin extends Plugin {

    public FastDrawingPlugin(PluginInformation info) {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            Main.map.addMapMode(new IconToggleButton(new FastDrawingMode(Main.map)));
        }
    }
}
