// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.layer.imagery;

import java.util.EventObject;

/**
 * Event fired when imagery filters settings change.
 */
public class FilterChangedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code FilterChangedEvent}.
     * @param source object on which the Event initially occurred
     */
    public FilterChangedEvent(Object source) {
        super(source);
    }
}
