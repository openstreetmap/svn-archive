// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.openstreetmap.josm.data.projection.Projection;

public class DefaultMifHandler extends DefaultGeographicHandler implements MifHandler {

    private Projection nonEarthProj;
    
    @Override
    public MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient)
            throws FactoryException {
        return null;
    }

    @Override
    public void setCoordSysNonEarthProjection(Projection p) {
        nonEarthProj = p;
    }

    @Override
    public Projection getCoordSysNonEarthProjection() {
        return nonEarthProj;
    }
}
