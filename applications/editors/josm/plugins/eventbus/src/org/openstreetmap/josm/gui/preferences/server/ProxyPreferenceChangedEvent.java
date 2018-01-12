// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.preferences.server;

import java.util.EventObject;

/**
 * Event fired when proxy settings are updated.
 */
public class ProxyPreferenceChangedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ProxyPreferenceChangedEvent}.
     * @param source object on which the Event initially occurred
     */
    public ProxyPreferenceChangedEvent(Object source) {
        super(source);
    }
}
