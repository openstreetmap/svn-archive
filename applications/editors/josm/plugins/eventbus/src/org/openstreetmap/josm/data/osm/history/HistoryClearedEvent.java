// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.osm.history;

/**
 * Event fired when history is cleared.
 */
public class HistoryClearedEvent extends AbstractHistoryEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code HistoryClearedEvent}.
     * @param source object on which the Event initially occurred
     * @param historyDataSet history data set for which the event is trigerred
     */
    public HistoryClearedEvent(Object source, HistoryDataSet historyDataSet) {
        super(source, historyDataSet);
    }
}
