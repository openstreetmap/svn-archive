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

/**
 * A ShapeHandler defines what is needed to construct and persist geometries
 * based upon the shapefile specification.
 * 
 * @author aaime
 * @author Ian Schneider
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/shp/ShapeHandler.java $
 * 
 */
public interface ShapeHandler {

	/**
     * Read a geometry from the ByteBuffer. The buffer's position, byteOrder,
     * and limit are set to that which is needed. The record has been read as
     * well as the shape type integer. The handler need not worry about reading
     * unused information as the ShapefileReader will correctly adjust the
     * buffer position after this call.
     * 
     * @param buffer
     *                The ByteBuffer to read from.
     * @return A geometry object.
     */
    public Object read(ByteBuffer buffer, ShapeType type, boolean flatGeometry);
}
