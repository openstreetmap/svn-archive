// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public abstract class DefaultGeographicHandler implements GeographicHandler {

    private boolean useNodeMap = true;
    private boolean checkNodeProximity = false;
    private boolean preferMultipolygonToSimpleWay = false;

    @Override
    public boolean preferMultipolygonToSimpleWay() {
        return preferMultipolygonToSimpleWay;
    }

    @Override
    public void setPreferMultipolygonToSimpleWay(boolean prefer) {
        preferMultipolygonToSimpleWay = prefer;
    }

    @Override
    public boolean checkNodeProximity() {
        return checkNodeProximity;
    }

    @Override
    public void setCheckNodeProximity(boolean check) {
        checkNodeProximity = check;
    }

    @Override
    public void setUseNodeMap(boolean use) {
        useNodeMap = use;
    }

    @Override
    public boolean useNodeMap() {
        return useNodeMap;
    }

    @Override
    public CoordinateReferenceSystem getCrsFor(String crsName) throws NoSuchAuthorityCodeException, FactoryException {
        if (crsName.equalsIgnoreCase("GCS_ETRS_1989")) {
            return CRS.decode("EPSG:4258");
        }
        return null;
    }
}
