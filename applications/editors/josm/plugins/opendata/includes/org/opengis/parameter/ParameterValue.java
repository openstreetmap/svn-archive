/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.parameter;

import java.net.URI;
import javax.measure.unit.Unit;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * A parameter value used by an operation method. Most parameter values are numeric, but
 * other types of parameter values are possible. The parameter type can be fetch with the
 * <code>{@linkplain #getValue()}.{@linkplain Object#getClass() getClass()}</code> idiom.
 * The {@link #getValue()} and {@link #setValue(Object)} methods can be invoked at any time.
 * Others getters and setters are parameter-type dependents.
 *
 * @param <T> The type of parameter values.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/parameter/ParameterValue.java $
 * @version <A HREF="http://portal.opengeospatial.org/files/?artifact_id=6716">Abstract specification 2.0</A>
 * @author  Martin Desruisseaux (IRD)
 * @author  Jody Garnett (Refractions Research)
 * @since   GeoAPI 1.0
 *
 * @see ParameterDescriptor
 * @see ParameterValueGroup
 */
@UML(identifier="CC_ParameterValue", specification=ISO_19111)
public interface ParameterValue<T> extends GeneralParameterValue {
    /**
     * Returns the abstract definition of this parameter value.
     *
     * @return The abstract definition of this parameter value.
     */
    ParameterDescriptor<T> getDescriptor();

    /**
     * Returns the unit of measure of the {@linkplain #doubleValue() parameter value}.
     * If the parameter value has no unit (for example because it is a {@link String} type),
     * then this method returns {@code null}. Note that "no unit" doesn't means
     * "dimensionless".
     *
     * @return The unit of measure of the parameter value.
     *
     * @see #doubleValue()
     * @see #doubleValueList
     * @see #getValue
     */
    Unit<?> getUnit();

    /**
     * Returns the numeric value of the coordinate operation parameter in the specified unit
     * of measure. This convenience method applies unit conversion on the fly as needed.
     *
     * @param  unit The unit of measure for the value to be returned.
     * @return The numeric value represented by this parameter after conversion to type
     *         {@code double} and conversion to {@code unit}.
     * @throws InvalidParameterTypeException if the value is not a numeric type.
     * @throws IllegalArgumentException if the specified unit is invalid for this parameter.
     *
     * @see #getUnit
     * @see #setValue(double,Unit)
     * @see #doubleValueList(Unit)
     */
    double doubleValue(Unit<?> unit) throws InvalidParameterTypeException;

    /**
     * Returns the numeric value of the coordinate operation parameter with its
     * associated {@linkplain #getUnit unit of measure}.
     *
     * @return The numeric value represented by this parameter after conversion to type {@code double}.
     * @throws InvalidParameterTypeException if the value is not a numeric type.
     * @unitof Measure
     *
     * @rename Renamed {@code value} to {@code doubleValue} for consistency with
     *         {@link Number#doubleValue} and the other {@code fooValue} in this interface.
     *         Also because {@link #getValue} is already used for an {@link Object} type, for
     *         consistency with {@link #setValue(Object)}.
     *
     * @see #getUnit
     * @see #setValue(double)
     * @see #doubleValueList
     */
    @UML(identifier="value", obligation=CONDITIONAL, specification=ISO_19111)
    double doubleValue() throws InvalidParameterTypeException;

    /**
     * Returns the positive integer value of an operation parameter, usually used
     * for a count. An integer value does not have an associated unit of measure.
     *
     * @return The numeric value represented by this parameter after conversion to type {@code int}.
     * @throws InvalidParameterTypeException if the value is not an integer type.
     *
     * @rename Renamed {@code integerValue} to {@code intValue} for consistency with
     *         {@link Number#intValue} and the Java primitive type {@code int}.
     *
     * @see #setValue(int)
     * @see #intValueList
     */
    @UML(identifier="integerValue", obligation=CONDITIONAL, specification=ISO_19111)
    int intValue() throws InvalidParameterTypeException;

    /**
     * Returns the parameter value as an object. The object type is typically a {@link Double},
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URI}, {@code double[]} or
     * {@code int[]}.
     *
     * @return The parameter value as an object.
     *
     * @see #setValue(Object)
     */
    @UML(identifier="value", obligation=CONDITIONAL, specification=ISO_19111)
    T getValue();

    /**
     * Sets the parameter value as an array of floating point and their associated unit.
     *
     * @param  values The parameter values.
     * @param  unit The unit for the specified value.
     * @throws InvalidParameterValueException if the floating point type is inappropriate for this
     *         parameter, or if the value is illegal for some other reason (for example a value out
     *         of range).
     */
    void setValue(double[] values, Unit<?> unit) throws InvalidParameterValueException;

    /**
     * Sets the parameter value as a floating point and its associated unit.
     *
     * @param  value The parameter value.
     * @param  unit The unit for the specified value.
     * @throws InvalidParameterValueException if the floating point type is inappropriate for this
     *         parameter, or if the value is illegal for some other reason (for example a value out
     *         of range).
     *
     * @see #setValue(double)
     * @see #doubleValue(Unit)
     */
    void setValue(double value, Unit<?> unit) throws InvalidParameterValueException;

    /**
     * Sets the parameter value as a floating point.
     *
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the floating point type is inappropriate for this
     *         parameter, or if the value is illegal for some other reason (for example a value out
     *         of range).
     *
     * @see #setValue(double,Unit)
     * @see #doubleValue()
     */
    void setValue(double value) throws InvalidParameterValueException;

    /**
     * Set the parameter value as an integer.
     *
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the integer type is inappropriate for this parameter,
     *         or if the value is illegal for some other reason (for example a value out of range).
     *
     * @see #intValue
     */
    void setValue(int value) throws InvalidParameterValueException;

    /**
     * Set the parameter value as an object. The object type is typically a {@link Double},
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URI}, {@code double[]}
     * or {@code int[]}.
     * <p>
     * The argument is not restricted to the parameterized type {@code T} because the type
     * is typically unknown (as in <code>group.{@linkplain ParameterValueGroup#parameter
     * parameter}("<var>name</var>").setValue(<var>value</var>)</code>) and
     * because some implementations may choose to convert a wider range of types.
     *
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the type of {@code value} is inappropriate
     *         for this parameter, or if the value is illegal for some other reason (for example
     *         the value is numeric and out of range).
     *
     * @see #getValue
     */
    void setValue(Object value) throws InvalidParameterValueException;

    /**
     * Returns a copy of this parameter value.
     *
     * @return A copy of this parameter value.
     */
    ParameterValue clone();
}
