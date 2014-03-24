// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Provides the map mode and controls visibility of the elevation profile layer/panel.
 */
public class ElevationMapMode extends MapMode implements IElevationModelListener {
    /**
     * 
     */
    private static final long serialVersionUID = -1011179566962655639L;


    public ElevationMapMode(String name, MapFrame mapFrame) {
        super(name,
                "elevation.png",
                tr("Shows elevation profile"),
                mapFrame,
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void elevationProfileChanged(IElevationProfile profile) {
        ElevationProfilePlugin.getCurrentLayer().setProfile(profile);
    }

    @Override
    public void enterMode() {
        super.enterMode();
        ElevationProfilePlugin.getCurrentLayer().setVisible(true);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        ElevationProfilePlugin.getCurrentLayer().setVisible(false);
    }
}
