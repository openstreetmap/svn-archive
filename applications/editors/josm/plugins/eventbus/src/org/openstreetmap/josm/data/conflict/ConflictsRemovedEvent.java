// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.conflict;

import java.util.Collection;

/**
 * Event fired when conflicts are removed.
 */
public class ConflictsRemovedEvent extends AbstractConflictsEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ConflictsRemovedEvent}.
     * @param source object on which the Event initially occurred
     * @param conflicts conflicts collection
     */
    public ConflictsRemovedEvent(Object source, ConflictCollection conflicts) {
        super(source, conflicts);
    }

    /**
     * Returns removed conflicts.
     * @return removed conflicts
     */
    public Collection<Conflict<?>> getRemovedConflicts() {
        throw new UnsupportedOperationException("Requires a change in core"); // FIXME
    }
}
