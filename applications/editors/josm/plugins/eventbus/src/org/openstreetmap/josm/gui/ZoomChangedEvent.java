// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui;

import java.util.EventObject;

/**
 * Event fired when the zoom changes.
 */
public class ZoomChangedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ZoomChangedEvent}.
     * @param source object on which the Event initially occurred
     */
    public ZoomChangedEvent(Object source) {
        super(source);
    }
}
