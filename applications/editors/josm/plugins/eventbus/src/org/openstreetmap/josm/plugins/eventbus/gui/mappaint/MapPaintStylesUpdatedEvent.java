// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui.mappaint;

import java.util.EventObject;

/**
 * Event fired when map paint styles are updated.
 */
public class MapPaintStylesUpdatedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code MapPaintStylesUpdatedEvent}.
     * @param source object on which the Event initially occurred
     */
    public MapPaintStylesUpdatedEvent(Object source) {
        super(source);
    }
}
