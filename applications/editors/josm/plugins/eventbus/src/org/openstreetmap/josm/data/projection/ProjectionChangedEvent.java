// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.projection;

import java.util.EventObject;
import java.util.Objects;

/**
 * Event fired when the map projection changes.
 */
public class ProjectionChangedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final Projection oldValue;
    private final Projection newValue;

    /**
     * Constructs a new {@code ProjectionChangedEvent}.
     * @param source object on which the Event initially occurred
     * @param oldValue old projection
     * @param newValue new projection
     */
    public ProjectionChangedEvent(Object source, Projection oldValue, Projection newValue) {
        super(source);
        this.oldValue = Objects.requireNonNull(oldValue);
        this.newValue = Objects.requireNonNull(newValue);
    }

    /**
     * Returns the old projection.
     * @return the old projection
     */
    public final Projection getOldValue() {
        return oldValue;
    }

    /**
     * Returns the new projection.
     * @return the new projection
     */
    public final Projection getNewValue() {
        return newValue;
    }
}
