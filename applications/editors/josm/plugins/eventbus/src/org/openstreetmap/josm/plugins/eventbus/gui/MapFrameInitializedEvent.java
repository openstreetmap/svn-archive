// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui;

import java.util.EventObject;

import org.openstreetmap.josm.gui.MapFrame;

/**
 * Event fired when the map frame is initialized (after the first data is loaded).
 */
public class MapFrameInitializedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final MapFrame oldFrame;
    private final MapFrame newFrame;

    /**
     * Constructs a new {@code MapFrameInitializedEvent}.
     * @param source object on which the Event initially occurred
     * @param oldFrame The old MapFrame
     * @param newFrame The new MapFrame
     */
    public MapFrameInitializedEvent(Object source, MapFrame oldFrame, MapFrame newFrame) {
        super(source);
        this.oldFrame = oldFrame;
        this.newFrame = newFrame;
    }

    /**
     * Returns the old MapFrame.
     * @return the old MapFrame
     */
    public MapFrame getOldMapFrame() {
        return oldFrame;
    }

    /**
     * Returns the new MapFrame.
     * @return the new MapFrame
     */
    public MapFrame getNewMapFrame() {
        return newFrame;
    }
}
