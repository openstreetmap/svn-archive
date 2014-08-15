// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.openstreetmap.josm.data.projection.Projection;

public interface MifHandler extends GeographicHandler {

    public void setCoordSysNonEarthProjection(Projection p);
    
    public Projection getCoordSysNonEarthProjection();
}
