// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.io;

import java.util.EventObject;

import org.openstreetmap.josm.io.OsmApi;

/**
 * Event fired when the OSM API is initialized.
 */
public class OsmApiInitializedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final OsmApi osmApi;

    /**
     * Constructs a new {@code OsmApiInitializedEvent}.
     * @param source object on which the Event initially occurred
     * @param osmApi OSM API
     */
    public OsmApiInitializedEvent(Object source, OsmApi osmApi) {
        super(source);
        this.osmApi = osmApi;
    }

    /**
     * Returns the OSM API.
     * @return the OSM API
     */
    public final OsmApi getOsmApi() {
        return osmApi;
    }
}
