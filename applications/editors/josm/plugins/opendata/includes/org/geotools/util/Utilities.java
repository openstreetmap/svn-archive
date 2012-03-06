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
package org.geotools.util;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import org.geotools.factory.Hints;


/**
 * Miscellaneous methods, including cnvenience methods for {@link Object#equals equals} and
 * {@link Object#hashCode hashCode} implementations. Example use case in a class called
 * {@code Car}:
 *
 * <pre>
 * public boolean equals(Object other) {
 *     if (this == aThat) {
 *         return true;
 *     }
 *     if (other == null || !other.getClass().equals(getClass())) {
 *         return false;
 *     }
 *     Car that = (Car) other;
 *     return Utilities.equals(this.name,              that.name)       &&
 *            Utilities.equals(this.numDoors,          that.numDoors)   &&
 *            Utilities.equals(this.gasMileage,        that.gasMileage) &&
 *            Utilities.equals(this.color,             that.color)      &&
 *            Arrays   .equals(this.maintenanceChecks, that.maintenanceChecks);
 * }
 * </pre>
 *
 * Note the usage of {@link Arrays} method for comparing arrays.
 * <p>
 * This class also provides convenience methods for computing {@linkplain Object#hashCode hash code}
 * values. All those methods expect a {@code seed} argument, which is the hash code value computed
 * for previous fields in a class. For the initial seed (the one for the field for which to compute
 * an hash code), an arbitrary value must be provided. We suggest a different number for different
 * class in order to reduce the risk of collision between "empty" instances of different classes.
 * {@linkplain java.io.Serializable} classes can use {@code (int) serialVersionUID} for example.
 *
 * @since 2.5
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/util/Utilities.java $
 * @version $Id: Utilities.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class Utilities {
    /**
     * An array of strings containing only white spaces. Strings' lengths are equal to their
     * index in the {@code spaces} array. For example, {@code spaces[4]}œcontains a string of
     * length 4. Strings are constructed only when first needed.
     */
    private static final String[] spaces = new String[21];
    static {
        final int last = spaces.length - 1;
        final char[] blancs = new char[last];
        Arrays.fill(blancs, ' ');
        spaces[last] = new String(blancs).intern();
    }

    /**
     * The singleton instance to be returned by {@link #emptyQueue}.
     */
    private static final Queue<?> EMPTY_QUEUE = new EmptyQueue<Object>();

    /**
     * The class for the {@link #EMPTY_QUEUE} instance. Defined as a named class rather than
     * anonymous in order to avoid serialization issue.
     */
    private static final class EmptyQueue<E> extends AbstractQueue<E> implements Serializable {
        /** For cross-version compatibility. **/
        private static final long serialVersionUID = -6147951199761870325L;

        /** No effect on an queue which is already empty. */
        @Override
        public void clear() {
        }

        /** Returns {@code true} is all case. */
        @Override
        public boolean isEmpty() {
            return true;
        }

        /** Returns the size, which is always 0. */
        public int size() {
            return 0;
        }

        /** Returns an empty iterator. */
        public Iterator<E> iterator() {
            final Set<E> empty = Collections.emptySet();
            return empty.iterator();
        }

        /** Always returns {@code false} since this queue doesn't accept any element. */
        public boolean offer(E e) {
            return false;
        }

        /** Always returns {@code null} since this queue is always empty. */
        public E poll() {
            return null;
        }

        /** Always returns {@code null} since this queue is always empty. */
        public E peek() {
            return null;
        }

        /** Returns the singleton instance of deserialization. */
        protected Object readResolve() {
            return EMPTY_QUEUE;
        }
    }

    /**
     * Forbid object creation.
     */
    private Utilities() {
    }

    /**
     * Returns {@code true} if the given doubles are equals. Positive and negative zero are
     * considered different, while a NaN value is considered equal to other NaN values.
     *
     * @param o1 The first value to compare.
     * @param o2 The second value to compare.
     * @return {@code true} if both values are equal.
     *
     * @see Double#equals
     */
    public static boolean equals(double o1, double o2) {
        if (Double.doubleToLongBits(o1) == Double.doubleToLongBits(o2))
            return true;
        
        double tol = getTolerance();
        final double min = o1 - Math.signum(o1) * o1 * tol; 
        final double max = o1 + Math.signum(o1) * o1 * tol;
        return min <= o2 && o2 <= max;
    }
    
    /**
     * Gathers the tolerance for floating point comparisons
     * @return The tolerance set in the hints, or its default value if not set
     */
    private static double getTolerance() {
        Double tol = ((Double) Hints.getSystemDefault(Hints.COMPARISON_TOLERANCE));
        if(tol == null)
            return Hints.COMPARISON_TOLERANCE.getDefault();
        else
            return tol;
    }
    
    /**
     * Convenience method for testing two objects for equality. One or both objects may be null.
     * This method do <strong>not</strong> iterates recursively in array elements. If array needs
     * to be compared, use one of {@link Arrays} method or {@link #deepEquals deepEquals} instead.
     * <p>
     * <b>Note on assertions:</b> There is no way to ensure at compile time that this method
     * is not invoked with array arguments, while doing so would usually be a program error.
     * Performing a systematic argument check would impose a useless overhead for correctly
     * implemented {@link Object#equals} methods. As a compromise we perform this check at runtime
     * only if assertions are enabled. Using assertions for argument check in a public API is
     * usually a deprecated practice, but we make an exception for this particular method.
     * <p>
     * <b>Note on method overloading:</b> This method could be selected by the compiler for
     * comparing primitive types, because the compiler could perform an auto-boxing and get
     * a result assignable to {@code Object}. However it should not occur in practice because
     * overloaded (and more efficient) methods are provided for every primitive types. This is
     * true even when the two arguments are different primitive type because of widening
     * conversions. The only exception is when a {@code boolean} argument is mixed with a
     * different primitive type.
     *
     * @param object1 The first object to compare, or {@code null}.
     * @param object2 The second object to compare, or {@code null}.
     * @return {@code true} if both objects are equal.
     * @throws AssertionError If assertions are enabled and at least one argument is an array.
     */
    public static boolean equals(final Object object1, final Object object2) throws AssertionError {
        assert object1 == null || !object1.getClass().isArray() : object1;
        assert object2 == null || !object2.getClass().isArray() : object2;
        return (object1 == object2) || (object1 != null && object1.equals(object2));
    }

    /**
     * Convenience method for testing two objects for equality. One or both objects may be null.
     * If both are non-null and are arrays, then every array elements will be compared.
     * <p>
     * This method may be useful when the objects may or may not be array. If they are known
     * to be arrays, consider using {@link Arrays#deepEquals(Object[],Object[])} or one of its
     * primitive counter-part instead.
     * <p>
     * <strong>Rules for choosing an {@code equals} or {@code deepEquals} method</strong>
     * <ul>
     *   <li>If <em>both</em> objects are declared as {@code Object[]} (not anything else like
     *   {@code String[]}), consider using {@link Arrays#deepEquals(Object[],Object[])} except
     *   if it is known that the array elements can never be other arrays.</li>
     *
     *   <li>Otherwise if both objects are arrays (e.g. {@code Expression[]}, {@code String[]},
     *   {@code int[]}, <cite>etc.</cite>), use {@link Arrays#equals(Object[],Object[])}. This
     *   rule is applicable to arrays of primitive type too, since {@code Arrays.equals} is
     *   overriden with primitive counter-parts.</li>
     *
     *   <li>Otherwise if at least one object is anything else than {@code Object} (e.g.
     *   {@code String}, {@code Expression}, <cite>etc.</cite>), use {@link #equals(Object,Object)}.
     *   Using this {@code deepEquals} method would be an overkill since there is no chance that
     *   {@code String} or {@code Expression} could be an array.</li>
     *
     *   <li>Otherwise if <em>both</em> objects are declared exactly as {@code Object} type and
     *   it is known that they could be arrays, only then invoke this {@code deepEquals} method.
     *   In such case, make sure that the hash code is computed using {@link #deepHashCode} for
     *   consistency.</li>
     * </ul>
     *
     * @param object1 The first object to compare, or {@code null}.
     * @param object2 The second object to compare, or {@code null}.
     * @return {@code true} if both objects are equal.
     */
    public static boolean deepEquals(final Object object1, final Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        if (object1 instanceof Object[]) {
            return (object2 instanceof Object[]) &&
                    Arrays.deepEquals((Object[]) object1, (Object[]) object2);
        }
        if (object1 instanceof double[]) {
            return (object2 instanceof double[]) &&
                    Arrays.equals((double[]) object1, (double[]) object2);
        }
        if (object1 instanceof float[]) {
            return (object2 instanceof float[]) &&
                    Arrays.equals((float[]) object1, (float[]) object2);
        }
        if (object1 instanceof long[]) {
            return (object2 instanceof long[]) &&
                    Arrays.equals((long[]) object1, (long[]) object2);
        }
        if (object1 instanceof int[]) {
            return (object2 instanceof int[]) &&
                    Arrays.equals((int[]) object1, (int[]) object2);
        }
        if (object1 instanceof short[]) {
            return (object2 instanceof short[]) &&
                    Arrays.equals((short[]) object1, (short[]) object2);
        }
        if (object1 instanceof byte[]) {
            return (object2 instanceof byte[]) &&
                    Arrays.equals((byte[]) object1, (byte[]) object2);
        }
        if (object1 instanceof char[]) {
            return (object2 instanceof char[]) &&
                    Arrays.equals((char[]) object1, (char[]) object2);
        }
        if (object1 instanceof boolean[]) {
            return (object2 instanceof boolean[]) &&
                    Arrays.equals((boolean[]) object1, (boolean[]) object2);
        }
        return object1.equals(object2);
    }

    /**
     * Returns a string of the specified length filled with white spaces.
     * This method tries to return a pre-allocated string if possible.
     *
     * @param  length The string length. Negative values are clamped to 0.
     * @return A string of length {@code length} filled with white spaces.
     */
    public static String spaces(int length) {
        /*
         * No need to synchronize.  In the unlikely event of two threads calling this method
         * at the same time and the two calls creating a new string, the String.intern() call
         * will take care of canonicalizing the strings.
         */
        if (length < 0) {
            length = 0;
        }
        String s;
        if (length < spaces.length) {
            s = spaces[length];
            if (s == null) {
                s = spaces[spaces.length - 1].substring(0, length).intern();
                spaces[length] = s;
            }
        } else {
            char[] blancs = new char[length];
            Arrays.fill(blancs, ' ');
            s = new String(blancs);
        }
        return s;
    }
}
