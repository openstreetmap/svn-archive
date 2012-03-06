/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

import java.util.Map;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.PassThroughOperation;


/**
 * A pass-through operation specifies that a subset of a coordinate tuple is subject to a specific
 * coordinate operation.
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/operation/DefaultPassThroughOperation.java $
 * @version $Id: DefaultPassThroughOperation.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultPassThroughOperation extends DefaultSingleOperation implements PassThroughOperation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4308173919747248695L;

    /**
     * Constructs a single operation from a set of properties and the given transform.
     * The properties given in argument follow the same rules than for the
     * {@link AbstractCoordinateOperation} constructor.
     *
     * @param  properties Set of properties. Should contains at least {@code "name"}.
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @param  operation The operation to apply on the subset of a coordinate tuple.
     * @param  transform The {@linkplain MathTransformFactory#createPassThroughTransform
     *                   pass through transform}.
     */
    public DefaultPassThroughOperation(final Map<String,?>            properties,
                                       final CoordinateReferenceSystem sourceCRS,
                                       final CoordinateReferenceSystem targetCRS,
                                       final Operation                 operation,
                                       final MathTransform             transform)
    {
        super(properties, sourceCRS, targetCRS, transform);
        ensureNonNull("operation", operation);
        ensureValidDimension(operation.getSourceCRS(), transform.getSourceDimensions());
        ensureValidDimension(operation.getTargetCRS(), transform.getTargetDimensions());
    }

    /**
     * Ensure that the dimension of the specified CRS is not greater than the specified value.
     */
    private static void ensureValidDimension(final CoordinateReferenceSystem crs, final int dim) {
        if (crs.getCoordinateSystem().getDimension() > dim) {
            throw new IllegalArgumentException(); // TODO: provides a localized message.
        }
    }
}
