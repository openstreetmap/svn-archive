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

import java.util.HashMap;
import java.util.Map;

import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.referencing.AbstractReferenceSystem;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.GeodeticDatum;


/**
 * A coordinate reference system based on an ellipsoidal approximation of the geoid; this provides
 * an accurate representation of the geometry of geographic features for a large portion of the
 * earth's surface.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link EllipsoidalCS Ellipsoidal}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/crs/DefaultGeographicCRS.java $
 * @version $Id: DefaultGeographicCRS.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultGeographicCRS extends AbstractSingleCRS implements GeographicCRS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 861224913438092335L;

    /**
     * A two-dimensional geographic coordinate reference system using WGS84 datum.
     * This CRS uses (<var>longitude</var>,<var>latitude</var>) ordinates with longitude values
     * increasing East and latitude values increasing North. Angular units are decimal degrees and
     * prime meridian is Greenwich.
     */
    public static final DefaultGeographicCRS WGS84;


    static {
        final Map<String,Object> properties = new HashMap<String,Object>(4);
        properties.put(NAME_KEY, "WGS84(DD)"); // Name used in WCS 1.0.
        final String[] alias = {
            "WGS84",
            "WGS 84"  // EPSG name.
        };
        properties.put(ALIAS_KEY, alias);
        properties.put(DOMAIN_OF_VALIDITY_KEY, ExtentImpl.WORLD);
        WGS84    = new DefaultGeographicCRS(properties, DefaultGeodeticDatum.WGS84,
                                            DefaultEllipsoidalCS.GEODETIC_2D);
        alias[1] = "WGS 84 (geographic 3D)"; // Replaces the EPSG name.
    }

    /**
     * Constructs a geographic CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultGeographicCRS(final Map<String,?> properties,
                                final GeodeticDatum datum,
                                final EllipsoidalCS cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Returns the coordinate system.
     */
    @Override
    public EllipsoidalCS getCoordinateSystem() {
        return (EllipsoidalCS) super.getCoordinateSystem();
    }

    /**
     * Returns the datum.
     */
    @Override
    public GeodeticDatum getDatum() {
        return (GeodeticDatum) super.getDatum();
    }

    /**
     * Returns a hash value for this geographic CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }
}
