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
package org.geotools.referencing.datum;

import java.util.Map;
import javax.measure.unit.Unit;
import javax.measure.quantity.Length;


/**
 * A ellipsoid which is spherical. This ellipsoid implements a faster
 * {@link #orthodromicDistance} method.
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/datum/Spheroid.java $
 * @version $Id: Spheroid.java 31000 2008-07-10 21:11:13Z desruisseaux $
 * @author Martin Desruisseaux (IRD)
 *
 * @since 2.0
 */
final class Spheroid extends DefaultEllipsoid {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7867565381280669821L;

    /**
     * Constructs a new sphere using the specified radius.
     *
     * @param properties    Set of properties. Should contains at least {@code "name"}.
     * @param radius        The equatorial and polar radius.
     * @param ivfDefinitive {@code true} if the inverse flattening is definitive.
     * @param unit          The units of the radius value.
     */
    protected Spheroid(Map<String,?> properties, double radius, boolean ivfDefinitive, Unit<Length> unit) {
        super(properties, check("radius", radius), radius, Double.POSITIVE_INFINITY, ivfDefinitive, unit);
    }
}
