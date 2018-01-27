// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.data;

import java.util.EventObject;

/**
 * Event fired when the command queue (undo/redo) size changes.
 */
public class CommandQueueEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final int queueSize;
    private final int redoSize;

    /**
     * Constructs a new {@code CommandQueueEvent}.
     * @param source object on which the Event initially occurred
     * @param queueSize Undo stack size
     * @param redoSize Redo stack size
     */
    public CommandQueueEvent(Object source, int queueSize, int redoSize) {
        super(source);
        this.queueSize = queueSize;
        this.redoSize = redoSize;
    }

    /**
     * Returns Undo stack size.
     * @return Undo stack size
     */
    public final int getQueueSize() {
        return queueSize;
    }

    /**
     * Returns Redo stack size.
     * @return Redo stack size
     */
    public final int getRedoSize() {
        return redoSize;
    }
}
