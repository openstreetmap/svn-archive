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

import org.geotools.referencing.AbstractReferenceSystem;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.datum.GeodeticDatum;


/**
 * A 3D coordinate reference system with the origin at the approximate centre of mass of the earth.
 * A geocentric CRS deals with the earth's curvature by taking a 3D spatial view, which obviates
 * the need to model the earth's curvature.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link CartesianCS Cartesian},
 *   {@link SphericalCS Spherical}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/crs/DefaultGeocentricCRS.java $
 * @version $Id: DefaultGeocentricCRS.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultGeocentricCRS extends AbstractSingleCRS implements GeocentricCRS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6784642848287659827L;

    /**
     * Constructs a geographic CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultGeocentricCRS(final Map<String,?> properties,
                                final GeodeticDatum datum,
                                final CartesianCS   cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Constructs a geographic CRS from a set of properties.
     * The properties are given unchanged to the
     * {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultGeocentricCRS(final Map<String,?> properties,
                                final GeodeticDatum datum,
                                final SphericalCS   cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Returns the datum.
     */
    @Override
    public GeodeticDatum getDatum() {
        return (GeodeticDatum) super.getDatum();
    }

    /**
     * Returns a hash value for this geocentric CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }
}
