// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.util;

import java.util.EventObject;

/**
 * Event fired when pressed extended modifier keys change is detected.
 */
public class ModifierExChangedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final int modifiers;

    /**
     * Constructs a new {@code ModifierExChangedEvent}.
     * @param source object on which the Event initially occurred
     * @param modifiers the new extended modifiers
     */
    public ModifierExChangedEvent(Object source, int modifiers) {
        super(source);
        this.modifiers = modifiers;
    }

    /**
     * Returns the new extended modifiers.
     * @return the new extended modifiers
     */
    public final int getModifiers() {
        return modifiers;
    }
}
