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
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.datum.TemporalDatum;


/**
 * A 1D coordinate reference system used for the recording of time.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link TimeCS Time}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/crs/DefaultTemporalCRS.java $
 * @version $Id: DefaultTemporalCRS.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultTemporalCRS extends AbstractSingleCRS implements TemporalCRS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3000119849197222007L;

    /**
     * Constructs a temporal CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param cs The coordinate system.
     * @param datum The datum.
     */
    public DefaultTemporalCRS(final Map<String,?> properties,
                              final TemporalDatum datum,
                              final TimeCS        cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Returns the coordinate system.
     */
    @Override
    public TimeCS getCoordinateSystem() {
        return (TimeCS) super.getCoordinateSystem();
    }

    /**
     * Returns the datum.
     */
    @Override
    public TemporalDatum getDatum() {
        return (TemporalDatum) super.getDatum();
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
