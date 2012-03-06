/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 1999-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.measure;


/**
 * A longitude angle. Positive longitudes are East, while negative longitudes are West.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/measure/Longitude.java $
 * @version $Id: Longitude.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (PMO, IRD)
 *
 * @see Latitude
 * @see AngleFormat
 */
public final class Longitude extends Angle {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8614900608052762636L;

    /**
     * Minimum legal value for longitude (-180°).
     */
    public static final double MIN_VALUE = -180;

    /**
     * Maximum legal value for longitude (+180°).
     */
    public static final double MAX_VALUE = +180;

    /**
     * Contruct a new longitude with the specified value.
     *
     * @param theta Angle in degrees.
     */
    public Longitude(final double theta) {
        super(theta);
    }
}
