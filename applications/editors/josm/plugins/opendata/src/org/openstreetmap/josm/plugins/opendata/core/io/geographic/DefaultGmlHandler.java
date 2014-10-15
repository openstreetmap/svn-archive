// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public class DefaultGmlHandler extends DefaultGeographicHandler implements GmlHandler {

    @Override
    public MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient)
            throws FactoryException {
        return CRS.findMathTransform(sourceCRS, targetCRS, lenient);
    }
}
