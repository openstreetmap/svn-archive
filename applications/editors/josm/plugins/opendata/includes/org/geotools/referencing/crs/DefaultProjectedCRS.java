/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.referencing.crs;

import java.util.Map;

import org.geotools.referencing.operation.DefiningConversion;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Projection;


/**
 * A 2D coordinate reference system used to approximate the shape of the earth on a planar surface.
 * It is done in such a way that the distortion that is inherent to the approximation is carefully
 * controlled and known. Distortion correction is commonly applied to calculated bearings and
 * distances to produce values that are a close match to actual field values.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link CartesianCS Cartesian}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/crs/DefaultProjectedCRS.java $
 * @version $Id: DefaultProjectedCRS.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultProjectedCRS extends AbstractDerivedCRS implements ProjectedCRS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4502680112031773028L;

    /**
     * Constructs a projected CRS from a set of properties. The properties are given unchanged
     * to the {@linkplain AbstractDerivedCRS#AbstractDerivedCRS(Map, OperationMethod,
     * CoordinateReferenceSystem, MathTransform, CoordinateSystem) super-class constructor}.
     *
     * @param  properties Name and other properties to give to the new derived CRS object and to
     *         the underlying {@linkplain org.geotools.referencing.operation.DefaultProjection
     *         projection}.
     * @param  method A description of the {@linkplain Conversion#getMethod method for the
     *         conversion}.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         {@code baseToDerived}.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         {@code baseToDeviced} don't match the dimension of {@code base}
     *         and {@code derivedCS} respectively.
     *
     * @deprecated Create explicitly a {@link DefiningConversion} instead.
     */
    public DefaultProjectedCRS(final Map<String,?>    properties,
                               final OperationMethod      method,
                               final GeographicCRS          base,
                               final MathTransform baseToDerived,
                               final CartesianCS       derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, method, base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a projected CRS from a {@linkplain DefiningConversion defining conversion}. The
     * properties are given unchanged to the {@linkplain AbstractDerivedCRS#AbstractDerivedCRS(Map,
     * Conversion, CoordinateReferenceSystem, MathTransform, CoordinateSystem) super-class constructor}.
     *
     * @param  properties Name and other properties to give to the new projected CRS object.
     * @param  conversionFromBase The {@linkplain DefiningConversion defining conversion}.
     * @param  base Coordinate reference system to base the projected CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the projected CRS. The number
     *         of axes must match the target dimension of the transform
     *         {@code baseToDerived}.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         {@code baseToDerived} don't match the dimension of {@code base}
     *         and {@code derivedCS} respectively.
     */
    public DefaultProjectedCRS(final Map<String,?>       properties,
                               final Conversion  conversionFromBase,
                               final GeographicCRS             base,
                               final MathTransform    baseToDerived,
                               final CartesianCS          derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, conversionFromBase, base, baseToDerived, derivedCS);
    }

    /**
     * Returns the coordinate system.
     */
    @Override
    public CartesianCS getCoordinateSystem() {
        return (CartesianCS) super.getCoordinateSystem();
    }

    /**
     * Returns the datum.
     */
    @Override
    public GeodeticDatum getDatum() {
        return (GeodeticDatum) super.getDatum();
    }

    /**
     * Returns the base coordinate reference system, which must be geographic.
     *
     * @return The base CRS.
     */
    @Override
    public GeographicCRS getBaseCRS() {
        return (GeographicCRS) super.getBaseCRS();
    }

    /**
     * Returns the map projection from the {@linkplain #getBaseCRS base CRS} to this CRS.
     *
     * @return The map projection from base CRS to this CRS.
     */
    @Override
    public Projection getConversionFromBase() {
        return (Projection) super.getConversionFromBase();
    }

    /**
     * Returns the expected type of conversion.
     */
    @Override
    Class<? extends Projection> getConversionType() {
        return Projection.class;
    }

    /**
     * Returns a hash value for this projected CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }
}
