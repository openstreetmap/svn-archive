// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.openstreetmap.josm.data.projection.Projection;

public interface MifHandler extends GeographicHandler {

    void setCoordSysNonEarthProjection(Projection p);

    Projection getCoordSysNonEarthProjection();
}
