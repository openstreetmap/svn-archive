// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui.dialogs.relation;

import java.util.EventObject;

/**
 * Event fired when a relation member becomes visible in the relation dialog.
 */
public class MemberVisibleEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final int index;

    /**
     * Constructs a new {@code MemberVisibleEvent}.
     * @param source object on which the Event initially occurred
     * @param index index of the member in the table
     */
    public MemberVisibleEvent(Object source, int index) {
        super(source);
        this.index = index;
    }

    /**
     * Returns member index in member table.
     * @return member index in member table
     */
    public final int getIndex() {
        return index;
    }
}
