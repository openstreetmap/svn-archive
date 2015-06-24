// License: GPL. For details, see LICENSE file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import java.util.Map;

/**
 * Interface for template tile sources, @see TemplatedTMSTileSource
 *
 * @author Wiktor Niesiobędzki
 * @since TODO
 */
public interface TemplatedTileSource extends TileSource {
    /**
     *
     * @return headers to be sent with http requests
     */
    public Map<String, String> getHeaders();
}
