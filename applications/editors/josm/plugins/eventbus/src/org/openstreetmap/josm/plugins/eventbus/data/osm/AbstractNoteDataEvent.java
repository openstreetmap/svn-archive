// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.data.osm;

import java.util.EventObject;
import java.util.Objects;

import org.openstreetmap.josm.data.osm.NoteData;

/**
 * Superclass of note events.
 */
abstract class AbstractNoteDataEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final NoteData noteData;

    /**
     * Constructs a new {@code AbstractNoteDataEvent}.
     * @param source object on which the Event initially occurred
     * @param noteData note data set
     */
    AbstractNoteDataEvent(Object source, NoteData noteData) {
        super(source);
        this.noteData = Objects.requireNonNull(noteData);
    }

    /**
     * Returns note data set.
     * @return note data set
     */
    public final NoteData getNoteData() {
        return noteData;
    }
}
