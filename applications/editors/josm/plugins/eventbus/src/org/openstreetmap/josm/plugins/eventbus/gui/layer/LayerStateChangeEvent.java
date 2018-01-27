// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui.layer;

import java.util.EventObject;
import java.util.Objects;

import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Event fired when the "upload discouraged" (upload=no) state changes.
 */
public class LayerStateChangeEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final OsmDataLayer osmDataLayer;
    private final boolean newValue;

    /**
     * Constructs a new {@code LayerStateChangeEvent}.
     * @param source object on which the Event initially occurred
     * @param layer data layer
     * @param newValue new value of the "upload discouraged" (upload=no) state
     */
    public LayerStateChangeEvent(Object source, OsmDataLayer layer, boolean newValue) {
        super(source);
        this.osmDataLayer = Objects.requireNonNull(layer);
        this.newValue = newValue;
    }

    /**
     * Returns the data layer.
     * @return the data layer
     */
    public final OsmDataLayer getDataLayer() {
        return osmDataLayer;
    }

    /**
     * Returns the "upload discouraged" (upload=no) state.
     * @return the "upload discouraged" (upload=no) state
     */
    public final boolean isUploadDiscouraged() {
        return newValue;
    }
}
