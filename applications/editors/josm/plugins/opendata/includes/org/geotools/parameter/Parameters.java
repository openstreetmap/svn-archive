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
 */
package org.geotools.parameter;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.measure.unit.Unit;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.logging.Logging;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Utility class for methods helping implementing, and working with the
 * parameter API from {@link org.opengis.parameter} package.
 * <p>
 * <h3>Design note</h3>
 * This class contains some methods working on a specific parameter in a group (e.g.
 * {@linkplain #search searching}, {@linkplain #ensureSet setting a value}, <cite>etc.</cite>).
 * Parameters are identified by their {@linkplain ParameterDescriptor#getName name} instead of
 * their full {@linkplain ParameterDescriptor descriptor} object, because:
 * <ul>
 *   <li>The parameter descriptor may not be always available. For example a user may looks for
 *       the {@code "semi_major"} axis length (because it is documented in OGC specification under
 *       that name) but doesn't know and doesn't care about who is providing the implementation. In
 *       such case, he doesn't have the parameter's descriptor. He only have the parameter's name,
 *       and creating a descriptor from that name (a descriptor independent of any implementation)
 *       is tedious.</li>.
 *   <li>Parameter descriptors are implementation-dependent. For example if a user searchs for
 *       the above-cited {@code "semi_major"} axis length using the {@linkplain
 *       org.geotools.referencing.operation.projection.MapProjection.AbstractProvider#SEMI_MAJOR
 *       Geotools's descriptor} for this parameter, we will fail to find this parameter in any
 *       alternative {@link ParameterValueGroup} implementations. This is against GeoAPI's
 *       inter-operability goal.</li>
 * </ul>
 * <p>
 * The above doesn't mean that parameter's descriptor should not be used. They are used for
 * inspecting meta-data about parameters, not as a key for searching parameters in a group.
 * Since each parameter's name should be unique in a given parameter group (because
 * {@linkplain ParameterDescriptor#getMaximumOccurs maximum occurs} is always 1 for single
 * parameter), the parameter name is a suffisient key.
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/parameter/Parameters.java $
 * @version $Id: Parameters.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux
 */
public final class Parameters {
    /**
     * Small number for floating point comparaisons.
     */
    private static final double EPS = 1E-8;

    /**
     * An empty parameter group. This group contains no parameters.
     */
    public static ParameterDescriptorGroup EMPTY_GROUP =
            new DefaultParameterDescriptorGroup("empty", // TODO: localize
            new GeneralParameterDescriptor[0]);

    /**
     * Do not allows instantiation of this utility class.
     */
    private Parameters() {
    }

    /**
     * Casts the given parameter descriptor to the given type. An exception is thrown
     * immediately if the parameter does not have the expected value class. This
     * is a helper method for type safety when using Java 5 parameterized types.
     *
     * @param <T> The expected value class.
     * @param  descriptor The descriptor to cast.
     * @param  type The expected value class.
     * @return The descriptor casted to the given type.
     * @throws ClassCastException if the given descriptor doesn't have the expected value class.
     *
     * @since 2.5
     */
    @SuppressWarnings("unchecked")
    public static <T> ParameterDescriptor<T> cast(ParameterDescriptor<?> descriptor, Class<T> type)
            throws ClassCastException
    {
        if (descriptor != null) {
            final Class<?> actual = descriptor.getValueClass();
            // We require a strict equality - not type.isAssignableFrom(actual) - because in
            // the later case we could have (to be strict) to return a <? extends T> type.
            if (!type.equals(actual)) {
                throw new ClassCastException(Errors.format(ErrorKeys.BAD_PARAMETER_TYPE_$2,
                        descriptor.getName().getCode(), actual));
            }
        }
        return (ParameterDescriptor) descriptor;
    }

    /**
     * Casts the given parameter value to the given type. An exception is thrown
     * immediately if the parameter does not have the expected value class. This
     * is a helper method for type safety when using Java 5 parameterized types.
     *
     * @param <T> The expected value class.
     * @param  value The value to cast.
     * @param  type The expected value class.
     * @return The value casted to the given type.
     * @throws ClassCastException if the given value doesn't have the expected value class.
     *
     * @since 2.5
     */
    @SuppressWarnings("unchecked")
    public static <T> ParameterValue<T> cast(final ParameterValue<?> value, final Class<T> type)
            throws ClassCastException
    {
        if (value != null) {
            final ParameterDescriptor descriptor = value.getDescriptor();
            final Class<?> actual = descriptor.getValueClass();
            if (!type.equals(actual)) { // Same comment than cast(ParameterDescriptor)...
                throw new ClassCastException(Errors.format(ErrorKeys.BAD_PARAMETER_TYPE_$2,
                        descriptor.getName().getCode(), actual));
            }
        }
        return (ParameterValue) value;
    }

    /**
     * Ensures that the specified parameter is set. The {@code value} is set if and only if
     * no value were already set by the user for the given {@code name}.
     * <p>
     * The {@code force} argument said what to do if the named parameter is already set. If the
     * value matches, nothing is done in all case. If there is a mismatch and {@code force} is
     * {@code true}, then the parameter is overridden with the specified {@code value}. Otherwise,
     * the parameter is left unchanged but a warning is logged with the {@link Level#FINE FINE}
     * level.
     *
     * @param parameters The set of projection parameters.
     * @param name       The parameter name to set.
     * @param value      The value to set, or to expect if the parameter is already set.
     * @param unit       The value unit.
     * @param force      {@code true} for forcing the parameter to the specified {@code value}
     *                   is case of mismatch.
     * @return {@code true} if the were a mismatch, or {@code false} if the parameters can be
     *         used with no change.
     */
    public static boolean ensureSet(final ParameterValueGroup parameters,
                                    final String name, final double value, final Unit<?> unit,
                                    final boolean force)
    {
        final ParameterValue<?> parameter;
        try {
            parameter = parameters.parameter(name);
        } catch (ParameterNotFoundException ignore) {
            /*
             * Parameter not found. This exception should not occurs most of the time.
             * If it occurs, we will not try to set the parameter here, but the same
             * exception is likely to occurs at MathTransform creation time. The later
             * is the expected place for this exception, so we will let it happen there.
             */
            return false;
        }
        try {
            if (Math.abs(parameter.doubleValue(unit) / value - 1) <= EPS) {
                return false;
            }
        } catch (InvalidParameterTypeException exception) {
            /*
             * The parameter is not a floating point value. Don't try to set it. An exception is
             * likely to be thrown at MathTransform creation time, which is the expected place.
             */
            return false;
        } catch (IllegalStateException exception) {
            /*
             * No value were set for this parameter, and there is no default value.
             */
            parameter.setValue(value, unit);
            return true;
        }
        /*
         * A value was set, but is different from the expected value.
         */
        if (force) {
            parameter.setValue(value, unit);
        } else {
            // TODO: localize
            final LogRecord record = new LogRecord(Level.FINE, "Axis length mismatch.");
            record.setSourceClassName(Parameters.class.getName());
            record.setSourceMethodName("ensureSet");
            final Logger logger = Logging.getLogger(Parameters.class);
            record.setLoggerName(logger.getName());
            logger.log(record);
        }
        return true;
    }
}
