/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * @author aaime
 * @author Ian Schneider
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/shp/MultiPointHandler.java $
 * 
 */
public class MultiPointHandler implements ShapeHandler {
    final ShapeType shapeType;
    GeometryFactory geometryFactory;

    public MultiPointHandler(ShapeType type, GeometryFactory gf) throws ShapefileException {
        if ((type != ShapeType.MULTIPOINT) && (type != ShapeType.MULTIPOINTM)
                && (type != ShapeType.MULTIPOINTZ)) {
            throw new ShapefileException(
                    "Multipointhandler constructor - expected type to be 8, 18, or 28");
        }

        shapeType = type;
        this.geometryFactory = gf;
    }
    
    private Object createNull() {
        Coordinate[] c = null;
        return geometryFactory.createMultiPoint(c);
    }

    public Object read(ByteBuffer buffer, ShapeType type, boolean flatGeometry) {
        if (type == ShapeType.NULL) {
            return createNull();
        }

        // read bounding box (not needed)
        buffer.position(buffer.position() + 4 * 8);

        int numpoints = buffer.getInt();
        int dimensions = shapeType == ShapeType.MULTIPOINTZ && !flatGeometry ? 3 : 2;
        CoordinateSequence cs = geometryFactory.getCoordinateSequenceFactory().create(numpoints, dimensions);

        DoubleBuffer dbuffer = buffer.asDoubleBuffer();
        double[] ordinates = new double[numpoints * 2];
        dbuffer.get(ordinates);
        for (int t = 0; t < numpoints; t++) {
            cs.setOrdinate(t, 0, ordinates[t * 2]);
            cs.setOrdinate(t, 1, ordinates[t * 2 + 1]);
        }

        if (dimensions > 2) {
            dbuffer.position(dbuffer.position() + 2);

            dbuffer.get(ordinates, 0, numpoints);
            for (int t = 0; t < numpoints; t++) {
                cs.setOrdinate(t, 2, ordinates[t]); // z
            }
        }

        return geometryFactory.createMultiPoint(cs);
    }
}
