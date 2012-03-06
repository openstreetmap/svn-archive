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
 * A latitude angle. Positive latitudes are North, while negative
 * latitudes are South. This class has no direct OpenGIS equivalent.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/measure/Latitude.java $
 * @version $Id: Latitude.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (PMO, IRD)
 *
 * @see Longitude
 * @see AngleFormat
 */
public final class Latitude extends Angle {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4496748683919618976L;

    /**
     * Minimum legal value for latitude (-90°).
     */
    public static final double MIN_VALUE = -90;

    /**
     * Maximum legal value for latitude (+90°).
     */
    public static final double MAX_VALUE = +90;

    /**
     * Contruct a new latitude with the specified value.
     *
     * @param theta Angle in degrees.
     */
    public Latitude(final double theta) {
        super(theta);
    }
}
