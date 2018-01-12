// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.conflict.tags;

import java.util.EventObject;

/**
 * Event fired when the focus goes to previous decision.
 */
public class PreviousDecisionEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code PreviousDecisionEvent}.
     * @param source object on which the Event initially occurred
     */
    public PreviousDecisionEvent(Object source) {
        super(source);
    }
}
