/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.net.URL;

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
 *
 *
 */
public class EditGpxPlugin extends Plugin {

    private IconToggleButton btn;
    private EditGpxMode mode;

    public EditGpxPlugin(PluginInformation info) {
        super(info);
        mode = new EditGpxMode(Main.map, "editgpx", tr("edit gpx tracks"));

        btn = new IconToggleButton(mode);
        btn.setVisible(true);
    }

    /**
     * initialize button. if button is pressed create new layer.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        mode.setFrame(newFrame);
        if (oldFrame == null && newFrame != null) {
            if (Main.map != null)
                Main.map.addMapMode(btn);
        }
    }

    public static ImageIcon loadIcon(String name) {
        URL url = EditGpxPlugin.class.getResource("/images/editgpx.png");
        return new ImageIcon(url);
    }
}
