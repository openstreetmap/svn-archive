// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gui;

import org.openstreetmap.josm.data.gpx.WayPoint;

public interface IElevationProfileSelectionListener {
    /**
     * Notifies clients about selected index changed.
     */
    void selectedWayPointChanged(WayPoint wpt);
}
