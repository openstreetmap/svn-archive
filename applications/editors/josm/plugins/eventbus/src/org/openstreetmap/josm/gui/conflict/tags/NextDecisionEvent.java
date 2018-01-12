// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.conflict.tags;

import java.util.EventObject;

/**
 * Event fired when the focus goes to next decision.
 */
public class NextDecisionEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code NextDecisionEvent}.
     * @param source object on which the Event initially occurred
     */
    public NextDecisionEvent(Object source) {
        super(source);
    }
}
