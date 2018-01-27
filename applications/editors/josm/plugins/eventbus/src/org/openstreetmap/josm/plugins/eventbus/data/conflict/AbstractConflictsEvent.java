// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.data.conflict;

import java.util.EventObject;
import java.util.Objects;

import org.openstreetmap.josm.data.conflict.ConflictCollection;

/**
 * Superclass of conflict events.
 */
public abstract class AbstractConflictsEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final ConflictCollection conflicts;

    /**
     * Constructs a new {@code ExpertModeChangedEvent}.
     * @param source object on which the Event initially occurred
     * @param conflicts collection of all conflicts
     */
    AbstractConflictsEvent(Object source, ConflictCollection conflicts) {
        super(source);
        this.conflicts = Objects.requireNonNull(conflicts);
    }

    /**
     * Returns collection of all conflicts.
     * @return collection of all conflicts
     */
    public ConflictCollection getConflicts() {
        return conflicts;
    }
}
