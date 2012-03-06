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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A set of miscellaneous methods working on {@link Class} objects.
 *
 * @since 2.5
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/resources/Classes.java $
 * @version $Id: Classes.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
@SuppressWarnings("unused")
public final class Classes {
    /**
     * Constants to be used in {@code switch} statements.
     */
    public static final byte DOUBLE=8, FLOAT=7, LONG=6, INTEGER=5, SHORT=4, BYTE=3,
                            CHARACTER=2, BOOLEAN=1, OTHER=0;

    /**
     * Mapping between a primitive type and its wrapper, if any.
     */
    private static final Map<Class<?>,Classes> MAPPING = new HashMap<Class<?>,Classes>(16);
    static {
        new Classes(Double   .TYPE, Double   .class, true,  false, (byte) Double   .SIZE, DOUBLE   );
        new Classes(Float    .TYPE, Float    .class, true,  false, (byte) Float    .SIZE, FLOAT    );
        new Classes(Long     .TYPE, Long     .class, false, true,  (byte) Long     .SIZE, LONG     );
        new Classes(Integer  .TYPE, Integer  .class, false, true,  (byte) Integer  .SIZE, INTEGER  );
        new Classes(Short    .TYPE, Short    .class, false, true,  (byte) Short    .SIZE, SHORT    );
        new Classes(Byte     .TYPE, Byte     .class, false, true,  (byte) Byte     .SIZE, BYTE     );
        new Classes(Character.TYPE, Character.class, false, false, (byte) Character.SIZE, CHARACTER);
        new Classes(Boolean  .TYPE, Boolean  .class, false, false, (byte) 1,              BOOLEAN  );
        new Classes(Void     .TYPE, Void     .class, false, false, (byte) 0,              OTHER    );
    }

    /** The wrapper for the primitive type.     */ private final Class<?> wrapper;
    /** {@code true} for integer number.        */ private final boolean  isInteger;

    /**
     * Creates a mapping between a primitive type and its wrapper.
     */
    private Classes(final Class<?> primitive, final Class<?> wrapper,
                    final boolean  isFloat,   final boolean  isInteger,
                    final byte     size,      final byte     ordinal)
    {
        this.wrapper   = wrapper;
        this.isInteger = isInteger;
        if (MAPPING.put(primitive, this) != null || MAPPING.put(wrapper, this) != null) {
            throw new AssertionError(); // Should never happen.
        }
    }

    /**
     * If the given method is a getter or a setter for a parameterized attribute, returns the
     * upper bounds of the parameterized type. Otherwise returns {@code null}. This method
     * provides the same semantic than {@link #boundOfParameterizedAttribute(Field)}, but
     * works on a getter or setter method rather then the field. See the javadoc of above
     * methods for more details.
     * <p>
     * This method is typically used for fetching the type of elements in a collection.
     * We do not provide a method working from a {@link Class} instance because of the
     * way parameterized types are implemented in Java (by erasure).
     *
     * @param  method The getter or setter method for which to obtain the parameterized type.
     * @return The upper bound of parameterized type, or {@code null} if the given method
     *         do not opperate on an object of a parameterized type.
     */
    public static Class<?> boundOfParameterizedAttribute(final Method method) {
        Class<?> c = getActualTypeArgument(method.getGenericReturnType());
        if (c == null) {
            final Type[] parameters = method.getGenericParameterTypes();
            if (parameters != null && parameters.length == 1) {
                c = getActualTypeArgument(parameters[0]);
            }
        }
        return c;
    }

    /**
     * Delegates to {@link ParameterizedType#getActualTypeArguments} and returns the result as a
     * {@link Class}, provided that every objects are of the expected classes and the result was
     * an array of length 1 (so there is no ambiguity). Otherwise returns {@code null}.
     */
    private static Class<?> getActualTypeArgument(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] p = ((ParameterizedType) type).getActualTypeArguments();
            while (p != null && p.length == 1) {
                type = p[0];
                if (type instanceof Class) {
                    return (Class) type;
                } else if (type instanceof WildcardType) {
                    p = ((WildcardType) type).getUpperBounds();
                } else {
                    break; // Unknown type.
                }
            }
        }
        return null;
    }

    /**
     * Returns the class of the specified object, or {@code null} if {@code object} is null.
     * This method is also useful for fetching the class of an object known only by its bound
     * type. As of Java 6, the usual pattern:
     *
     * <blockquote><pre>
     * Number n = 0;
     * Class<? extends Number> c = n.getClass();
     * </pre></blockquote>
     *
     * doesn't seem to work if {@link Number} is replaced by a parametirez type {@code T}.
     *
     * @param  <T> The type of the given object.
     * @param  object The object for which to get the class, or {@code null}.
     * @return The class of the given object, or {@code null} if the given object was null.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getClass(final T object) {
        return (object != null) ? (Class<? extends T>) object.getClass() : null;
    }

    /**
     * Returns {@code true} if the two specified objects implements exactly the same set of
     * interfaces. Only interfaces assignable to {@code base} are compared. Declaration order
     * doesn't matter. For example in ISO 19111, different interfaces exist for different coordinate
     * system geometries ({@code CartesianCS}, {@code PolarCS}, etc.). We can check if two
     * CS implementations has the same geometry with the following code:
     *
     * <blockquote><code>
     * if (sameInterfaces(cs1, cs2, {@linkplain org.opengis.referencing.cs.CoordinateSystem}.class))
     * </code></blockquote>
     *
     * @param <T>     A common parent for both objects.
     * @param object1 The first object to check for interfaces.
     * @param object2 The second object to check for interfaces.
     * @param base    The parent of all interfaces to check.
     * @return        {@code true} if both objects implement the same set of interfaces,
     *                considering only sub-interfaces of {@code base}.
     */
    public static <T> boolean sameInterfaces(final Class<? extends T> object1,
                                             final Class<? extends T> object2,
                                             final Class<T> base)
    {
        if (object1 == object2) {
            return true;
        }
        if (object1==null || object2==null) {
            return false;
        }
        final Class<?>[] c1 = object1.getInterfaces();
        final Class<?>[] c2 = object2.getInterfaces();
        /*
         * Trim all interfaces that are not assignable to 'base' in the 'c2' array.
         * Doing this once will avoid to redo the same test many time in the inner
         * loops j=[0..n].
         */
        int n = 0;
        for (int i=0; i<c2.length; i++) {
            final Class<?> c = c2[i];
            if (base.isAssignableFrom(c)) {
                c2[n++] = c;
            }
        }
        /*
         * For each interface assignable to 'base' in the 'c1' array, check if
         * this interface exists also in the 'c2' array. Order doesn't matter.
         */
compare:for (int i=0; i<c1.length; i++) {
            final Class<?> c = c1[i];
            if (base.isAssignableFrom(c)) {
                for (int j=0; j<n; j++) {
                    if (c.equals(c2[j])) {
                        System.arraycopy(c2, j+1, c2, j, --n-j);
                        continue compare;
                    }
                }
                return false; // Interface not found in 'c2'.
            }
        }
        return n == 0; // If n>0, at least one interface was not found in 'c1'.
    }

    /**
     * Returns {@code true} if the given {@code type} is an integer type.
     *
     * @param  type The type to test (may be {@code null}).
     * @return {@code true} if {@code type} is the primitive of wrapper class of
     *         {@link Long}, {@link Integer}, {@link Short} or {@link Byte}.
     */
    public static boolean isInteger(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) && mapping.isInteger;
    }

    /**
     * Changes a primitive class to its wrapper (e.g. {@code int} to {@link Integer}).
     * If the specified class is not a primitive type, then it is returned unchanged.
     *
     * @param  type The primitive type (may be {@code null}).
     * @return The type as a wrapper.
     */
    public static Class<?> primitiveToWrapper(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) ? mapping.wrapper : type;
    }

    /**
     * Converts the specified string into a value object. The value object can be an instance of
     * {@link Double}, {@link Float}, {@link Long}, {@link Integer}, {@link Short}, {@link Byte},
     * {@link Boolean}, {@link Character} or {@link String} according the specified type. This
     * method is intentionnaly restricted to primitive types, with the addition of {@code String}
     * which can be though as an identity operation. Other types like {@link java.io.File} are
     * not the purpose of this method.
     *
     * @param  <T> The requested type.
     * @param  type The requested type.
     * @param  value the value to parse.
     * @return The value object, or {@code null} if {@code value} was null.
     * @throws IllegalArgumentException if {@code type} is not a recognized type.
     * @throws NumberFormatException if {@code type} is a subclass of {@link Number} and the
     *         string value is not parseable as a number of the specified type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(final Class<T> type, final String value)
            throws IllegalArgumentException, NumberFormatException
    {
        if (value == null) {
            return null;
        }
        if (Double .class.equals(type)) return (T) Double .valueOf(value);
        if (Float  .class.equals(type)) return (T) Float  .valueOf(value);
        if (Long   .class.equals(type)) return (T) Long   .valueOf(value);
        if (Integer.class.equals(type)) return (T) Integer.valueOf(value);
        if (Short  .class.equals(type)) return (T) Short  .valueOf(value);
        if (Byte   .class.equals(type)) return (T) Byte   .valueOf(value);
        if (Boolean.class.equals(type)) return (T) Boolean.valueOf(value);
        if (Character.class.equals(type)) {
            /*
             * If the string is empty, returns 0 which means "end of string" in C/C++
             * and NULL in Unicode standard. If non-empty, take only the first char.
             * This is somewhat consistent with Boolean.valueOf(...) which is quite
             * lenient about the parsing as well, and throwing a NumberFormatException
             * for those would not be appropriate.
             */
            return (T) Character.valueOf(value.length() != 0 ? value.charAt(0) : 0);
        }
        if (String.class.equals(type)) {
            return (T) value;
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1, type));
    }

    /**
     * Returns a short class name for the specified class. This method will
     * omit the package name.  For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object. It will also name
     * array according Java language usage,  for example "double[]" instead
     * of "[D".
     *
     * @param  classe The object class (may be {@code null}).
     * @return A short class name for the specified object.
     */
    public static String getShortName(Class<?> classe) {
        if (classe == null) {
            return "<*>";
        }
        String name = classe.getSimpleName();
        Class<?> enclosing = classe.getEnclosingClass();
        if (enclosing != null) {
            final StringBuilder buffer = new StringBuilder();
            do {
                buffer.insert(0, '.').insert(0, enclosing.getSimpleName());
            } while ((enclosing = enclosing.getEnclosingClass()) != null);
            name = buffer.append(name).toString();
        }
        return name;
    }

    /**
     * Returns a short class name for the specified object. This method will
     * omit the package name. For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object.
     *
     * @param  object The object (may be {@code null}).
     * @return A short class name for the specified object.
     */
    public static String getShortClassName(final Object object) {
        return getShortName(getClass(object));
    }
}
