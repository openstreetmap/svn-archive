// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.osm.history;

import java.util.EventObject;
import java.util.Objects;

/**
 * Superclass of history events.
 */
public class AbstractHistoryEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final HistoryDataSet historyDataSet;

    /**
     * Constructs a new {@code AbstractHistoryEvent}.
     * @param source object on which the Event initially occurred
     * @param historyDataSet history data set for which the event is trigerred
     */
    AbstractHistoryEvent(Object source, HistoryDataSet historyDataSet) {
        super(source);
        this.historyDataSet = Objects.requireNonNull(historyDataSet);
    }

    /**
     * Returns history data set for which the event is trigerred.
     * @return history data set for which the event is trigerred
     */
    public final HistoryDataSet getHistoryDataSet() {
        return historyDataSet;
    }
}
