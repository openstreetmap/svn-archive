// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.conflict;

import java.util.Collection;

/**
 * Event fired when conflicts are added.
 */
public class ConflictsAddedEvent extends AbstractConflictsEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ConflictsAddedEvent}.
     * @param source object on which the Event initially occurred
     * @param conflicts conflicts collection
     */
    public ConflictsAddedEvent(Object source, ConflictCollection conflicts) {
        super(source, conflicts);
    }

    /**
     * Returns added conflicts.
     * @return added conflicts
     */
    public Collection<Conflict<?>> getAddedConflicts() {
        throw new UnsupportedOperationException("Requires a change in core"); // FIXME
    }
}
