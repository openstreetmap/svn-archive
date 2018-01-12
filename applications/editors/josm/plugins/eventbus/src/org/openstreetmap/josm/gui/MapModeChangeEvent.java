// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui;

import java.util.EventObject;

import org.openstreetmap.josm.actions.mapmode.MapMode;

/**
 * Event fired when the map mode changes.
 */
public class MapModeChangeEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final MapMode oldMapMode;
    private final MapMode newMapMode;

    /**
     * Constructs a new {@code MapModeChangeEvent}.
     * @param source object on which the Event initially occurred
     * @param oldMapMode old map mode
     * @param newMapMode new map mode
     */
    public MapModeChangeEvent(Object source, MapMode oldMapMode, MapMode newMapMode) {
        super(source);
        this.oldMapMode = oldMapMode;
        this.newMapMode = newMapMode;
    }

    /**
     * Returns the old map mode.
     * @return the old map mode
     */
    public final MapMode getOldMapMode() {
        return oldMapMode;
    }

    /**
     * Returns the new map mode.
     * @return the new map mode
     */
    public final MapMode getNewMapMode() {
        return newMapMode;
    }
}
