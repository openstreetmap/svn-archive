// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.http2;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.HttpClient;

/**
 * Provides HTTP/2 support.
 */
public class Http2Plugin extends Plugin {

    /**
     * Constructs a new {@code Http2Plugin}.
     * @param info plugin information
     */
    public Http2Plugin(PluginInformation info) {
        super(info);
        HttpClient.setFactory(Http2Client::new);
    }
}
