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

import java.util.Date;
import java.util.Map;

import org.geotools.referencing.AbstractIdentifiedObject;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.util.InternationalString;


/**
 * A temporal datum defines the origin of a temporal coordinate reference system.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/datum/DefaultTemporalDatum.java $
 * @version $Id: DefaultTemporalDatum.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 *
 * @since 2.1
 */
public class DefaultTemporalDatum extends AbstractDatum implements TemporalDatum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3357241732140076884L;

    /**
     * The date and time origin of this temporal datum.
     */
    private final long origin;

    /**
     * Constructs a temporal datum from a set of properties. The properties map is given
     * unchanged to the {@linkplain AbstractDatum#AbstractDatum(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param origin The date and time origin of this temporal datum.
     */
    public DefaultTemporalDatum(final Map<String,?> properties, final Date origin) {
        super(properties);
        ensureNonNull("origin", origin);
        this.origin = origin.getTime();
    }

    /**
     * The date and time origin of this temporal datum.
     *
     * @return The date and time origin of this temporal datum.
     */
    public Date getOrigin() {
        return new Date(origin);
    }

    /**
     * Description of the point or points used to anchor the datum to the Earth.
     */
    @Override
    public InternationalString getAnchorPoint() {
        return super.getAnchorPoint();
    }

    /**
     * The time after which this datum definition is valid.
     */
    @Override
    public Date getRealizationEpoch() {
        return super.getRealizationEpoch();
    }

    /**
     * Compare this temporal datum with the specified object for equality.
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
            final DefaultTemporalDatum that = (DefaultTemporalDatum) object;
            return this.origin == that.origin;
        }
        return false;
    }

    /**
     * Returns a hash value for this temporal datum. {@linkplain #getName Name},
     * {@linkplain #getRemarks remarks} and the like are not taken in account. In
     * other words, two temporal datums will return the same hash value if they
     * are equal in the sense of
     * <code>{@link #equals equals}(AbstractIdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ (int)origin ^ (int)(origin >>> 32);
    }
}
