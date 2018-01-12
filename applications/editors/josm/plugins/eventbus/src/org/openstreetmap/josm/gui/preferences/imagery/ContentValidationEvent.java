// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.preferences.imagery;

import java.util.EventObject;

/**
 * Event fired when the validation status of the "add imagery" panel changes.
 */
public class ContentValidationEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final boolean isValid;

    /**
     * Constructs a new {@code ContentValidationEvent}.
     * @param source object on which the Event initially occurred
     * @param isValid true if the conditions required to close this panel are met
     */
    public ContentValidationEvent(Object source, boolean isValid) {
        super(source);
        this.isValid = isValid;
    }

    /**
     * Determines if the conditions required to close this panel are met.
     * @return true if the conditions required to close this panel are met
     */
    public final boolean isValid() {
        return isValid;
    }
}
