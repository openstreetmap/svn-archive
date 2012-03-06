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
package org.geotools.referencing.cs;

import java.util.Map;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.util.InternationalString;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.geometry.MismatchedDimensionException;

import org.geotools.measure.Measure;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * A one-dimensional coordinate system containing a single time axis, used to describe the
 * temporal position of a point in the specified time units from a specified time origin.
 * A {@code TimeCS} shall have one {@linkplain #getAxis axis}.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.DefaultTemporalCRS Temporal}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/cs/DefaultTimeCS.java $
 * @version $Id: DefaultTimeCS.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultTimeCS extends AbstractCS implements TimeCS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5222911412381303989L;

    /**
     * A one-dimensional temporal CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#TIME time}</var>,
     * axis in {@linkplain javax.measure.unit.NonSI#DAY day} units.
     *
     * @see org.geotools.referencing.crs.DefaultTemporalCRS#JULIAN
     * @see org.geotools.referencing.crs.DefaultTemporalCRS#MODIFIED_JULIAN
     * @see org.geotools.referencing.crs.DefaultTemporalCRS#TRUNCATED_JULIAN
     * @see org.geotools.referencing.crs.DefaultTemporalCRS#DUBLIN_JULIAN
     */
    public static final DefaultTimeCS DAYS;

    /**
     * Creates the constants, reusing some intermediate constructs for efficienty.
     */
    static {
        final Map<String,Object> properties = name(VocabularyKeys.TEMPORAL);
        CoordinateSystemAxis axis = DefaultCoordinateSystemAxis.TIME;
        DAYS = new DefaultTimeCS(properties, axis);
        final InternationalString name = axis.getAlias().iterator().next().toInternationalString();
        axis = new DefaultCoordinateSystemAxis(name, "t", AxisDirection.FUTURE, SI.SECOND);
        axis = new DefaultCoordinateSystemAxis(name, "t", AxisDirection.FUTURE, SI.MILLI(SI.SECOND));
    }

    /**
     * Constructs a coordinate system from a set of properties.
     * The properties map is given unchanged to the
     * {@linkplain AbstractCS#AbstractCS(Map,CoordinateSystemAxis[]) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param axis       The axis.
     */
    public DefaultTimeCS(final Map<String,?> properties, final CoordinateSystemAxis axis) {
        super(properties, new CoordinateSystemAxis[] {axis});
        ensureTimeUnit(getAxis(0).getUnit());
    }

    /**
     * Returns {@code true} if the specified axis direction is allowed for this coordinate
     * system. The default implementation accepts only temporal directions (i.e.
     * {@link AxisDirection#FUTURE FUTURE} and {@link AxisDirection#PAST PAST}).
     */
    @Override
    protected boolean isCompatibleDirection(final AxisDirection direction) {
        return AxisDirection.FUTURE.equals(direction.absolute());
    }

    /**
     * Returns {@code true} if the specified unit is compatible with {@linkplain SI#SECOND seconds}.
     * This method is invoked at construction time for checking units compatibility.
     *
     * @since 2.2
     */
    @Override
    protected boolean isCompatibleUnit(final AxisDirection direction, final Unit<?> unit) {
        return SI.SECOND.isCompatible(unit);
    }

    /**
     * Computes the time difference between two points.
     *
     * @param  coord1 Coordinates of the first point.
     * @param  coord2 Coordinates of the second point.
     * @return The time difference between {@code coord1} and {@code coord2}.
     * @throws MismatchedDimensionException if a coordinate doesn't have the expected dimension.
     */
    @Override
    public Measure distance(final double[] coord1, final double[] coord2)
            throws MismatchedDimensionException
    {
        ensureDimensionMatch("coord1", coord1);
        ensureDimensionMatch("coord2", coord2);
        return new Measure(Math.abs(coord1[0] - coord2[0]), getDistanceUnit());
    }
}
