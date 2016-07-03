// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public interface GeographicHandler {

    void setPreferMultipolygonToSimpleWay(boolean prefer);

    boolean preferMultipolygonToSimpleWay();

    void setCheckNodeProximity(boolean check);

    boolean checkNodeProximity();

    void setUseNodeMap(boolean use);

    boolean useNodeMap();

    CoordinateReferenceSystem getCrsFor(String crsName) throws NoSuchAuthorityCodeException, FactoryException;

    MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient)
            throws FactoryException;
}
