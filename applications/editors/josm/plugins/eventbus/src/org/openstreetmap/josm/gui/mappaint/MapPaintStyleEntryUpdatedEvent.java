// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.mappaint;

import java.util.EventObject;

/**
 * Event fired when a map paint style entry is updated.
 */
public class MapPaintStyleEntryUpdatedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final int index;

    /**
     * Constructs a new {@code MapPaintStyleEntryUpdatedEvent}.
     * @param source object on which the Event initially occurred
     * @param index entry index
     */
    public MapPaintStyleEntryUpdatedEvent(Object source, int index) {
        super(source);
        this.index = index;
    }

    /**
     * Returns entry index.
     * @return entry index
     */
    public final int getIndex() {
        return index;
    }
}
