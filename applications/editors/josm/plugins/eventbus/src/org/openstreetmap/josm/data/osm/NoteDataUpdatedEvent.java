// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.osm;

/**
 * Event fired when note data set is updated.
 */
public class NoteDataUpdatedEvent extends AbstractNoteDataEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code NoteDataUpdatedEvent}.
     * @param source object on which the Event initially occurred
     * @param noteData note data set
     */
    public NoteDataUpdatedEvent(Object source, NoteData noteData) {
        super(source, noteData);
    }
}
