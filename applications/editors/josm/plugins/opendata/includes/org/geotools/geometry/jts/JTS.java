/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.geometry.jts;

import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;


/**
 * JTS Geometry utility methods, bringing Geotools to JTS.
 * <p>
 * Offers geotools based services such as reprojection.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>transformation</li>
 *   <li>coordinate sequence editing</li>
 *   <li>common coordinate sequence implementations for specific uses</li>
 * </ul>
 *
 * @since 2.2
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/geometry/jts/JTS.java $
 * @version $Id: JTS.java 37744 2011-07-29 08:59:05Z mbedward $
 * @author Jody Garnett
 * @author Martin Desruisseaux
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class JTS {

    /**
     * Do not allow instantiation of this class.
     */
    private JTS() {
    }

    /**
     * Transforms the geometry using the default transformer.
     *
     * @param  geom The geom to transform
     * @param  transform the transform to use during the transformation.
     * @return the transformed geometry.  It will be a new geometry.
     * @throws MismatchedDimensionException if the geometry doesn't have the expected dimension
     *         for the specified transform.
     * @throws TransformException if a point can't be transformed.
     */
    public static Geometry transform(final Geometry geom, final MathTransform transform)
        throws MismatchedDimensionException, TransformException {
        final GeometryCoordinateSequenceTransformer transformer = new GeometryCoordinateSequenceTransformer();
        transformer.setMathTransform(transform);

        return transformer.transform(geom);
    }
}
