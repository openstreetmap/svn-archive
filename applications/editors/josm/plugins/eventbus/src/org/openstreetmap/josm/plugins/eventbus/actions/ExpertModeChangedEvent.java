// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.actions;

import java.util.EventObject;

/**
 * Event fired whenever the expert mode setting changed.
 */
public class ExpertModeChangedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final boolean isExpert;

    /**
     * Constructs a new {@code ExpertModeChangedEvent}.
     * @param source object on which the Event initially occurred
     * @param isExpert {@code true} if expert mode has been enabled, false otherwise
     */
    public ExpertModeChangedEvent(Object source, boolean isExpert) {
        super(source);
        this.isExpert = isExpert;
    }

    /**
     * Determines if expert mode has been enabled.
     * @return {@code true} if expert mode has been enabled, false otherwise
     */
    public boolean isExpert() {
        return isExpert;
    }
}
