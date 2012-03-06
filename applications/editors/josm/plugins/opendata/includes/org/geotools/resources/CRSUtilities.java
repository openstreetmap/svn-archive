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
 */
package org.geotools.resources;

import java.util.List;

import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;


/**
 * A set of static methods working on OpenGIS&reg;
 * {@linkplain CoordinateReferenceSystem coordinate reference system} objects.
 * Some of those methods are useful, but not really rigorous. This is why they
 * do not appear in the "official" package, but instead in this private one.
 * <strong>Do not rely on this API!</strong> It may change in incompatible way
 * in any future release.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/resources/CRSUtilities.java $
 * @version $Id: CRSUtilities.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class CRSUtilities {
    /**
     * Do not allow creation of instances of this class.
     */
    private CRSUtilities() {
    }

    /**
     * Returns the components of the specified CRS, or {@code null} if none.
     */
    private static List<CoordinateReferenceSystem> getComponents(CoordinateReferenceSystem crs) {
        if (crs instanceof CompoundCRS) {
            final List<CoordinateReferenceSystem> components;
            components = ((CompoundCRS) crs).getCoordinateReferenceSystems();
            if (!components.isEmpty()) {
                return components;
            }
        }
        return null;
    }

    /**
     * Returns the datum of the specified CRS, or {@code null} if none.
     *
     * @param  crs The coordinate reference system for which to get the datum. May be {@code null}.
     * @return The datum in the given CRS, or {@code null} if none.
     */
    public static Datum getDatum(final CoordinateReferenceSystem crs) {
        return (crs instanceof SingleCRS) ? ((SingleCRS) crs).getDatum() : null;
    }

    /**
     * Returns the ellipsoid used by the specified coordinate reference system, providing that
     * the two first dimensions use an instance of {@link GeographicCRS}. Otherwise (i.e. if the
     * two first dimensions are not geographic), returns {@code null}.
     *
     * @param  crs The coordinate reference system for which to get the ellipsoid.
     * @return The ellipsoid in the given CRS, or {@code null} if none.
     */
    public static Ellipsoid getHeadGeoEllipsoid(CoordinateReferenceSystem crs) {
        while (!(crs instanceof GeographicCRS)) {
            final List<CoordinateReferenceSystem> c = getComponents(crs);
            if (c == null) {
                return null;
            }
            crs = c.get(0);
        }
        return ((GeographicCRS) crs).getDatum().getEllipsoid();
    }
}
