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

import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.referencing.datum.EngineeringDatum;


/**
 * Defines the origin of an engineering coordinate reference system. An engineering datum is used
 * in a region around that origin. This origin can be fixed with respect to the earth (such as a
 * defined point at a construction site), or be a defined point on a moving vehicle (such as on a
 * ship or satellite).
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/datum/DefaultEngineeringDatum.java $
 * @version $Id: DefaultEngineeringDatum.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultEngineeringDatum extends AbstractDatum implements EngineeringDatum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1498304918725248637L;

    /**
     * An engineering datum for unknow coordinate reference system. Such CRS are usually
     * assumed cartesian, but will not have any transformation path to other CRS.
     *
     * @see org.geotools.referencing.crs.DefaultEngineeringCRS#CARTESIAN_2D
     * @see org.geotools.referencing.crs.DefaultEngineeringCRS#CARTESIAN_3D
     */
    public static final DefaultEngineeringDatum UNKNOW =
            new DefaultEngineeringDatum(name(VocabularyKeys.UNKNOW));

    /**
     * Constructs an engineering datum from a set of properties. The properties map is given
     * unchanged to the {@linkplain AbstractDatum#AbstractDatum(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     */
    public DefaultEngineeringDatum(final Map<String,?> properties) {
        super(properties);
    }

    /**
     * Compare this datum with the specified object for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        return super.equals(object, compareMetadata);
    }
}
