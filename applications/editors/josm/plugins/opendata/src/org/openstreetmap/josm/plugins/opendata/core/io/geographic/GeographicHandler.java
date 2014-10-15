// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public interface GeographicHandler {
    
    public void setPreferMultipolygonToSimpleWay(boolean prefer);

    public boolean preferMultipolygonToSimpleWay();

    public void setCheckNodeProximity(boolean check);
    
    public boolean checkNodeProximity();
    
    public void setUseNodeMap(boolean use);
    
    public boolean useNodeMap();
    
    public CoordinateReferenceSystem getCrsFor(String crsName) throws NoSuchAuthorityCodeException, FactoryException;

    public MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient) throws FactoryException;
}
