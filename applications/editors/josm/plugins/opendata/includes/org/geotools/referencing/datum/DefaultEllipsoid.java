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
 *    This class contains formulas from the public FTP area of NOAA.
 *    NOAAS's work is fully acknowledged here.
 */
package org.geotools.referencing.datum;

import java.util.Collections;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Utilities;
import org.opengis.referencing.datum.Ellipsoid;


/**
 * Geometric figure that can be used to describe the approximate shape of the earth.
 * In mathematical terms, it is a surface formed by the rotation of an ellipse about
 * its minor axis. An ellipsoid requires two defining parameters:
 * <ul>
 *   <li>{@linkplain #getSemiMajorAxis semi-major axis} and
 *       {@linkplain #getInverseFlattening inverse flattening}, or</li>
 *   <li>{@linkplain #getSemiMajorAxis semi-major axis} and
 *       {@linkplain #getSemiMinorAxis semi-minor axis}.</li>
 * </ul>
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/datum/DefaultEllipsoid.java $
 * @version $Id: DefaultEllipsoid.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class DefaultEllipsoid extends AbstractIdentifiedObject implements Ellipsoid {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1149451543954764081L;

    /**
     * WGS 1984 ellipsoid with axis in {@linkplain SI#METER metres}. This ellipsoid is used
     * in GPS systems and is the default for most {@code org.geotools} packages.
     */
    public static final DefaultEllipsoid WGS84 =
            createFlattenedSphere("WGS84", 6378137.0, 298.257223563, SI.METER);

    /**
     * GRS 80 ellipsoid with axis in {@linkplain SI#METER metres}.
     *
     * @since 2.2
     */
    public static final DefaultEllipsoid GRS80 = // NO_UCD
            createFlattenedSphere("GRS80", 6378137.0, 298.257222101, SI.METER);

    /**
     * International 1924 ellipsoid with axis in {@linkplain SI#METER metres}.
     */
    public static final DefaultEllipsoid INTERNATIONAL_1924 = // NO_UCD
            createFlattenedSphere("International 1924", 6378388.0, 297.0, SI.METER);

    /**
     * Clarke 1866 ellipsoid with axis in {@linkplain SI#METER metres}.
     *
     * @since 2.2
     */
    public static final DefaultEllipsoid CLARKE_1866 = // NO_UCD
            createFlattenedSphere("Clarke 1866", 6378206.4, 294.9786982, SI.METER);

    /**
     * A sphere with a radius of 6371000 {@linkplain SI#METER metres}. Spheres use a simplier
     * algorithm for {@linkplain #orthodromicDistance orthodromic distance computation}, which
     * may be faster and more robust.
     */
    public static final DefaultEllipsoid SPHERE = // NO_UCD
            createEllipsoid("SPHERE", 6371000, 6371000, SI.METER);

    /**
     * The equatorial radius.
     * @see #getSemiMajorAxis
     */
    private final double semiMajorAxis;

    /**
     * The polar radius.
     * @see #getSemiMinorAxis
     */
    private final double semiMinorAxis;

    /**
     * The inverse of the flattening value, or {@link Double#POSITIVE_INFINITY}
     * if the ellipsoid is a sphere.
     *
     * @see #getInverseFlattening
     */
    private final double inverseFlattening;

    /**
     * Tells if the Inverse Flattening definitive for this ellipsoid.
     *
     * @see #isIvfDefinitive
     */
    private final boolean ivfDefinitive;

    /**
     * The units of the semi-major and semi-minor axis values.
     */
    private final Unit<Length> unit;

    /**
     * Constructs a new ellipsoid using the specified axis length. The properties map is
     * given unchanged to the {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties        Set of properties. Should contains at least {@code "name"}.
     * @param semiMajorAxis     The equatorial radius.
     * @param semiMinorAxis     The polar radius.
     * @param inverseFlattening The inverse of the flattening value.
     * @param ivfDefinitive     {@code true} if the inverse flattening is definitive.
     * @param unit              The units of the semi-major and semi-minor axis values.
     *
     * @see #createEllipsoid
     * @see #createFlattenedSphere
     */
    protected DefaultEllipsoid(final Map<String,?> properties,
                               final double  semiMajorAxis,
                               final double  semiMinorAxis,
                               final double  inverseFlattening,
                               final boolean ivfDefinitive,
                               final Unit<Length> unit)
    {
        super(properties);
        this.unit = unit;
        this.semiMajorAxis     = check("semiMajorAxis",     semiMajorAxis);
        this.semiMinorAxis     = check("semiMinorAxis",     semiMinorAxis);
        this.inverseFlattening = check("inverseFlattening", inverseFlattening);
        this.ivfDefinitive     = ivfDefinitive;
        ensureNonNull("unit", unit);
        ensureLinearUnit(unit);
    }

    /**
     * Constructs a new ellipsoid using the specified axis length.
     *
     * @param name          The ellipsoid name.
     * @param semiMajorAxis The equatorial radius.
     * @param semiMinorAxis The polar radius.
     * @param unit          The units of the semi-major and semi-minor axis values.
     * @return An ellipsoid with the given axis length.
     */
    public static DefaultEllipsoid createEllipsoid(final String name,
                                                   final double semiMajorAxis,
                                                   final double semiMinorAxis,
                                                   final Unit<Length> unit)
    {
        return createEllipsoid(Collections.singletonMap(NAME_KEY, name),
                               semiMajorAxis, semiMinorAxis, unit);
    }

    /**
     * Constructs a new ellipsoid using the specified axis length. The properties map is
     * given unchanged to the {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties    Set of properties. Should contains at least {@code "name"}.
     * @param semiMajorAxis The equatorial radius.
     * @param semiMinorAxis The polar radius.
     * @param unit          The units of the semi-major and semi-minor axis values.
     * @return An ellipsoid with the given axis length.
     */
    public static DefaultEllipsoid createEllipsoid(final Map<String,?> properties,
                                                   final double semiMajorAxis,
                                                   final double semiMinorAxis,
                                                   final Unit<Length> unit)
    {
        if (semiMajorAxis == semiMinorAxis) {
            return new Spheroid(properties, semiMajorAxis, false, unit);
        } else {
            return new DefaultEllipsoid(properties, semiMajorAxis, semiMinorAxis,
                       semiMajorAxis/(semiMajorAxis-semiMinorAxis), false, unit);
        }
    }

    /**
     * Constructs a new ellipsoid using the specified axis length and inverse flattening value.
     *
     * @param name              The ellipsoid name.
     * @param semiMajorAxis     The equatorial radius.
     * @param inverseFlattening The inverse flattening value.
     * @param unit              The units of the semi-major and semi-minor axis
     *                          values.
     * @return An ellipsoid with the given axis length.
     */
    public static DefaultEllipsoid createFlattenedSphere(final String name,
                                                         final double semiMajorAxis,
                                                         final double inverseFlattening,
                                                         final Unit<Length> unit)
    {
        return createFlattenedSphere(Collections.singletonMap(NAME_KEY, name),
                                     semiMajorAxis, inverseFlattening, unit);
    }

    /**
     * Constructs a new ellipsoid using the specified axis length and
     * inverse flattening value. The properties map is given unchanged to the
     * {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map) super-class constructor}.
     *
     * @param properties        Set of properties. Should contains at least {@code "name"}.
     * @param semiMajorAxis     The equatorial radius.
     * @param inverseFlattening The inverse flattening value.
     * @param unit              The units of the semi-major and semi-minor axis
     *                          values.
     * @return An ellipsoid with the given axis length.
     */
    public static DefaultEllipsoid createFlattenedSphere(final Map<String,?> properties,
                                                         final double semiMajorAxis,
                                                         final double inverseFlattening,
                                                         final Unit<Length> unit)
    {
        if (Double.isInfinite(inverseFlattening)) {
            return new Spheroid(properties, semiMajorAxis, true, unit);
        } else {
            return new DefaultEllipsoid(properties, semiMajorAxis,
                                        semiMajorAxis*(1-1/inverseFlattening),
                                        inverseFlattening, true, unit);
        }
    }

    /**
     * Checks the argument validity. Argument {@code value} should be greater than zero.
     *
     * @param  name  Argument name.
     * @param  value Argument value.
     * @return {@code value}.
     * @throws IllegalArgumentException if {@code value} is not greater than  0.
     */
    static double check(final String name, final double value) throws IllegalArgumentException {
        if (value > 0) {
            return value;
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2, name, value));
    }

    /**
     * Returns the linear unit of the {@linkplain #getSemiMajorAxis semi-major}
     * and {@linkplain #getSemiMinorAxis semi-minor} axis values.
     *
     * @return The axis linear unit.
     */
    public Unit<Length> getAxisUnit() {
        return unit;
    }

    /**
     * Length of the semi-major axis of the ellipsoid. This is the
     * equatorial radius in {@linkplain #getAxisUnit axis linear unit}.
     *
     * @return Length of semi-major axis.
     */
    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    /**
     * Length of the semi-minor axis of the ellipsoid. This is the
     * polar radius in {@linkplain #getAxisUnit axis linear unit}.
     *
     * @return Length of semi-minor axis.
     */
    public double getSemiMinorAxis() {
        return semiMinorAxis;
    }

    /**
     * Compare this ellipsoid with the specified object for equality.
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
            final DefaultEllipsoid that = (DefaultEllipsoid) object;
            return (!compareMetadata || this.ivfDefinitive == that.ivfDefinitive)   &&
                   Utilities.equals(this.semiMajorAxis,     that.semiMajorAxis)     &&
                   Utilities.equals(this.semiMinorAxis,     that.semiMinorAxis)     &&
                   Utilities.equals(this.inverseFlattening, that.inverseFlattening) &&
                   Utilities.equals(this.unit,              that.unit);
        }
        return false;
    }

    /**
     * Returns a hash value for this ellipsoid. {@linkplain #getName Name},
     * {@linkplain #getRemarks remarks} and the like are not taken in account.
     * In other words, two ellipsoids will return the same hash value if they
     * are equal in the sense of
     * <code>{@link #equals equals}(AbstractIdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        long longCode = 37*Double.doubleToLongBits(semiMajorAxis);
        if (ivfDefinitive) {
            longCode += inverseFlattening;
        } else {
            longCode += semiMinorAxis;
        }
        return (((int)(longCode >>> 32)) ^ (int)longCode);
    }
}
