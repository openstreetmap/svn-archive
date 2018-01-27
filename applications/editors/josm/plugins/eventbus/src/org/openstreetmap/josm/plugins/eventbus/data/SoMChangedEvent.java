// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.data;

import java.util.EventObject;
import java.util.Objects;

/**
 * Event fired when the syste of measurement changes.
 */
public class SoMChangedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final String oldSoM;
    private final String newSoM;

    /**
     * Constructs a new {@code SoMChangedEvent}.
     * @param source object on which the Event initially occurred
     * @param oldSoM old system of measurement
     * @param newSoM new system of measurement
     */
    public SoMChangedEvent(Object source, String oldSoM, String newSoM) {
        super(source);
        this.oldSoM = Objects.requireNonNull(oldSoM);
        this.newSoM = Objects.requireNonNull(newSoM);
    }

    /**
     * Returns the old system of measurement.
     * @return the old system of measurement
     */
    public final String getOldSoM() {
        return oldSoM;
    }

    /**
     * Returns the new system of measurement.
     * @return the new system of measurement
     */
    public final String getNewSoM() {
        return newSoM;
    }
}
