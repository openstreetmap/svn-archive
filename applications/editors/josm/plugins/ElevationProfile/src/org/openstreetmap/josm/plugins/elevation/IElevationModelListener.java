// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation;

import org.openstreetmap.josm.plugins.elevation.gpx.ElevationModel;

/**
 * This interface is intended to allow clients reaction on changes in the elevation model changes (e. g.
 * repaint UI widgets).
 * {@link ElevationModel}
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public interface IElevationModelListener {
    /**
     * Notifies listeners that the selected track has been changed.
     * @param model The model changed.
     */
    void elevationProfileChanged(IElevationProfile model);
}
