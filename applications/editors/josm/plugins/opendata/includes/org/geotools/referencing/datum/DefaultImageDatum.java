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
package org.geotools.referencing.datum;

import java.util.Map;

import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.util.Utilities;
import org.opengis.referencing.datum.ImageDatum;
import org.opengis.referencing.datum.PixelInCell;


/**
 * Defines the origin of an image coordinate reference system. An image datum is used in a local
 * context only. For an image datum, the anchor point is usually either the centre of the image
 * or the corner of the image.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/datum/DefaultImageDatum.java $
 * @version $Id: DefaultImageDatum.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 *
 * @since 2.1
 */
public class DefaultImageDatum extends AbstractDatum implements ImageDatum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4304193511244150936L;

    /**
     * Specification of the way the image grid is associated with the image data attributes.
     */
    private final PixelInCell pixelInCell;

    /**
     * Constructs an image datum from a set of properties. The properties map is given
     * unchanged to the {@linkplain AbstractDatum#AbstractDatum(Map) super-class constructor}.
     *
     * @param properties  Set of properties. Should contains at least {@code "name"}.
     * @param pixelInCell the way the image grid is associated with the image data attributes.
     */
    public DefaultImageDatum(final Map<String,?> properties, final PixelInCell pixelInCell) {
        super(properties);
        this.pixelInCell = pixelInCell;
        ensureNonNull("pixelInCell", pixelInCell);
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
        if (super.equals(object, compareMetadata)) {
            final DefaultImageDatum that = (DefaultImageDatum) object;
            return Utilities.equals(this.pixelInCell, that.pixelInCell);
        }
        return false;
    }

    /**
     * Returns a hash value for this image datum. {@linkplain #getName Name},
     * {@linkplain #getRemarks remarks} and the like are not taken in account. In
     * other words, two image datums will return the same hash value if they
     * are equal in the sense of
     * <code>{@link #equals equals}(AbstractIdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ pixelInCell.hashCode();
    }
}
