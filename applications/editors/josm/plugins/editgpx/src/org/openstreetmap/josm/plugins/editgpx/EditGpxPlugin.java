/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Provides an editable GPX layer. Editable layer here means the deletion of points is supported.
 * This plugin can be used to prepare tracks for upload to OSM eg. delete uninteresting parts
 * of the track.
 * Additionally while converting the track back to a normal GPX layer the time can be made
 * anonymous. This feature sets all time stamps to 1970-01-01 00:00.
 *
 * TODO:
 * - BUG: when importing eGpxLayer is shown as RawGpxLayer??
 * - implement reset if user made mistake while marking
 */
public class EditGpxPlugin extends Plugin {

    /**
     * Constructs a new {@code EditGpxPlugin}.
     * @param info plugin information
     */
    public EditGpxPlugin(PluginInformation info) {
        super(info);
    }

    /**
     * initialize button. if button is pressed create new layer.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            EditGpxMode mode = new EditGpxMode(newFrame);
            if (Main.map != null)
                Main.map.addMapMode(new IconToggleButton(mode));
        }
    }

    public static ImageIcon loadIcon(String name) {
        return new ImageIcon(EditGpxPlugin.class.getResource("/images/editgpx.png"));
    }
}
