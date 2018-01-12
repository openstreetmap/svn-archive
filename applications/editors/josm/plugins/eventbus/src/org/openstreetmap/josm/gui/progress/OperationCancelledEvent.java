// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.progress;

import java.util.EventObject;

/**
 * Event fired when a task is cancelled.
 */
public class OperationCancelledEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code OperationCancelledEvent}.
     * @param source object on which the Event initially occurred
     */
    public OperationCancelledEvent(Object source) {
        super(source);
    }
}
