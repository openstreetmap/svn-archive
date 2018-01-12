// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.util;

import java.awt.event.KeyEvent;
import java.util.EventObject;

/**
 * Event fired when a key is pressed.
 */
public class KeyPressedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final KeyEvent keyEvent;

    /**
     * Constructs a new {@code KeyPressedEvent}.
     * @param source object on which the Event initially occurred
     * @param keyEvent the original Swing key event
     */
    public KeyPressedEvent(Object source, KeyEvent keyEvent) {
        super(source);
        this.keyEvent = keyEvent;
    }

    /**
     * Returns the original Swing key event.
     * @return the original Swing key event
     */
    public final KeyEvent getKeyEvent() {
        return keyEvent;
    }
}
