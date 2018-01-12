// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.osm.history;

import org.openstreetmap.josm.data.osm.PrimitiveId;

/**
 * Event fired when history is updated for an OSM primitive.
 */
public class HistoryUpdatedEvent extends AbstractHistoryEvent {

    private static final long serialVersionUID = 1L;

    private final PrimitiveId id;

    /**
     * Constructs a new {@code HistoryUpdatedEvent}.
     * @param source object on which the Event initially occurred
     * @param historyDataSet history data set for which the event is trigerred
     * @param id the primitive id for which history has been updated
     */
    public HistoryUpdatedEvent(Object source, HistoryDataSet historyDataSet, PrimitiveId id) {
        super(source, historyDataSet);
        this.id = id;
    }

    /**
     * Returns the primitive id for which history has been updated.
     * @return primitive id for which history has been updated
     */
    public PrimitiveId getId() {
        return id;
    }
}
