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

import java.util.Date;


/**
 * A central place to register transformations between an arbitrary class and a
 * {@link Number}. For example, it is sometime convenient to consider {@link Date}
 * objects as if they were {@link Long} objects for computation purpose in generic
 * algorithms. Client can call the following method to convert an arbitrary object
 * to a {@link Number}:
 *
 * <blockquote><pre>
 * Object someArbitraryObject = new Date();
 * Number myObjectAsANumber = {@link ClassChanger#toNumber ClassChanger.toNumber}(someArbitraryObject);
 * </pre></blockquote>
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/resources/ClassChanger.java $
 * @version $Id: ClassChanger.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public abstract class ClassChanger<S extends Comparable<S>, T extends Number> {

    /**
     * A list of class objects that can be converted to numbers. This list is initialized
     * to a few commons {@link ClassChanger} instances for some standard Java classes like
     * {@link Date}. More objects can be added dynamically. This list must be <u>ordered</u>:
     * subclasses must be listed before parent classes.
     */
    private static ClassChanger<?,?>[] changers = new ClassChanger[] {
        new ClassChanger<Date,Long>(Date.class, Long.class) {
            protected Long convert(final Date object) {
                return object.getTime();
            }

            protected Date inverseConvert(final Long value) {
                return new Date(value.longValue());
            }
        }
    };

    /**
     * Parent class for {@link #convert}'s input objects.
     */
    private final Class<S> source;

    /**
     * Parent class for {@link #convert}'s output objects.
     */
    private final Class<T> target;

    /**
     * Constructs a new class changer.
     *
     * @param source Parent class for {@link #convert}'s input objects.
     * @param target Parent class for {@link #convert}'s output objects.
     */
    protected ClassChanger(final Class<S> source, final Class<T> target) {
        this.source = source;
        this.target = target;
        if (!Comparable.class.isAssignableFrom(source)) {
            throw new IllegalArgumentException(String.valueOf(source));
        }
        if (!Number.class.isAssignableFrom(target)) {
            throw new IllegalArgumentException(String.valueOf(target));
        }
    }

    /**
     * Returns the numerical value for an object.
     *
     * @param  object Object to convert (may be null).
     * @return The object's numerical value.
     * @throws ClassCastException if {@code object} is not of the expected class.
     */
    protected abstract T convert(final S object) throws ClassCastException;

    /**
     * Returns an instance of the converted classe from a numerical value.
     *
     * @param  value The value to wrap.
     * @return An instance of the source classe.
     */
    protected abstract S inverseConvert(final T value);

    /**
     * Returns a string representation for this class changer.
     */
    @Override
    public String toString() {
        return "ClassChanger[" + source.getName() + "\u00A0\u21E8\u00A0" + target.getName() + ']';
    }

    /**
     * Registers a new converter. All registered {@link ClassChanger} will
     * be taken in account by the {@link #toNumber} method. The example below
     * register a conversion for the {@link Date} class:
     *
     * <blockquote><pre>
     * &nbsp;ClassChanger.register(new ClassChanger(Date.class, Long.class) {
     * &nbsp;    protected Long convert(final Comparable o) {
     * &nbsp;        return ((Date) o).getTime();
     * &nbsp;    }
     * &nbsp;
     * &nbsp;    protected Comparable inverseConvert(final Number number) {
     * &nbsp;        return new Date(number.longValue());
     * &nbsp;    }
     * &nbsp;});
     * </pre></blockquote>
     *
     * @param  converter The {@link ClassChanger} to add.
     * @throws IllegalStateException if an other {@link ClassChanger} was already
     *         registered for the same {@code source} class. This is usually
     *         not a concern since the registration usually take place during the
     *         class initialization ("static" constructor).
     */
    public static synchronized void register(final ClassChanger<?,?> converter)
            throws IllegalStateException
    {
        int i;
        for (i=0; i<changers.length; i++) {
            if (changers[i].source.isAssignableFrom(converter.source)) {
                /*
                 * We found a converter for a parent class. The new converter should be
                 * inserted before its parent.  But before the insertion, we will check
                 * if this converter was not already registered later in the array.
                 */
                for (int j=i; j<changers.length; j++) {
                    if (changers[j].source.equals(converter.source)) {
                        throw new IllegalStateException(changers[j].toString());
                    }
                }
                break;
            }
        }
        changers = XArray.insert(changers, i, 1);
        changers[i] = converter;
    }
}
