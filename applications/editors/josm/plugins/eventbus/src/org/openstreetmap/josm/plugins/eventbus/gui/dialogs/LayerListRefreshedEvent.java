// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui.dialogs;

import java.util.EventObject;

/**
 * Event fired when the list of layers is refreshed.
 */
public class LayerListRefreshedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code LayerListRefreshedEvent}.
     * @param source object on which the Event initially occurred
     */
    public LayerListRefreshedEvent(Object source) {
        super(source);
    }
}
