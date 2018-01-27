// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui.dialogs;

import java.util.EventObject;
import java.util.Objects;

import org.openstreetmap.josm.gui.layer.Layer;

/**
 * Event fired when a layer becomes visible.
 */
public class LayerVisibleEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final int index;
    private final Layer layer;

    /**
     * Constructs a new {@code LayerVisibleEvent}.
     * @param source object on which the Event initially occurred
     * @param index layer index
     * @param layer layer
     */
    public LayerVisibleEvent(Object source, int index, Layer layer) {
        super(source);
        this.index = index;
        this.layer = Objects.requireNonNull(layer);
    }

    /**
     * Returns the layer index.
     * @return the layer index
     */
    public final int getIndex() {
        return index;
    }

    /**
     * Returns the layer.
     * @return the layer
     */
    public final Layer getLayer() {
        return layer;
    }
}
