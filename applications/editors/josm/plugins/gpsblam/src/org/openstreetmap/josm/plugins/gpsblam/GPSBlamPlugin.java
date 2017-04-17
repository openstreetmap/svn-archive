// License: GPL. Copyright (C) 2012 Russell Edwards
package org.openstreetmap.josm.plugins.gpsblam;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * GPSBlam is a JOSM plugin designed to exploit the availability of multiple GPS tracks over the same straight-line path,
 * to obtain a handle on the location and direction of the path with optimal accuracy.
 * This can be useful to nail down any offsets in existing content or imagery.
 */
public class GPSBlamPlugin extends Plugin {

    /**
     * Constructs a new {@code GPSBlamPlugin}.
     * @param info plugin info
     */
    public GPSBlamPlugin(PluginInformation info) {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            GPSBlamMode mode = new GPSBlamMode("gpsblam", tr("select gpx points and \"blam!\", find centre and direction of spread"));
            IconToggleButton btn = new IconToggleButton(mode);
            btn.setVisible(true);
            newFrame.addMapMode(btn);
        }
    }
}
