// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation;

import org.openstreetmap.josm.plugins.elevation.grid.EleVertex;

public interface IEleRenderingListener {

    /**
     * Notifies client that an elevation vertex has been finished for rendering.
     *
     * @param vertex the vertex
     */
    public void finished(EleVertex vertex);

    /**
     * Notifies a client that all vertices can be rendered now.
     */
    public void finishedAll();
}
