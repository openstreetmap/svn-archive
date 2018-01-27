// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.data.osm;

import org.openstreetmap.josm.data.osm.NoteData;

/**
 * Event fired when the selected note changes.
 */
public class SelectedNoteChangedEvent extends AbstractNoteDataEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code SelectedNoteChangedEvent}.
     * @param source object on which the Event initially occurred
     * @param noteData note data set
     */
    public SelectedNoteChangedEvent(Object source, NoteData noteData) {
        super(source, noteData);
    }
}
