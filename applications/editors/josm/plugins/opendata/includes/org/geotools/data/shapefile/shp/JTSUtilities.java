/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.shapefile.shp;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

/**
 * A collection of utility methods for use with JTS and the shapefile package.
 * 
 * @author aaime
 * @author Ian Schneider
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/shp/JTSUtilities.java $
 */
public class JTSUtilities {

    private JTSUtilities() {
    }

    public static final Class findBestGeometryClass(ShapeType type) {
        Class best;
        if (type == null || type == ShapeType.NULL) {
            best = Geometry.class;
        } else if (type.isLineType()) {
            best = MultiLineString.class;
        } else if (type.isMultiPointType()) {
            best = MultiPoint.class;
        } else if (type.isPointType()) {
            best = Point.class;
        } else if (type.isPolygonType()) {
            best = MultiPolygon.class;
        } else {
            throw new RuntimeException("Unknown ShapeType->GeometryClass : "
                    + type);
        }
        return best;
    }
}
