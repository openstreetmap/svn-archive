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

import java.lang.reflect.Array;
import java.util.Arrays;


/**
 * Simple operations on arrays. This class provides a central place for
 * inserting and deleting elements in an array, as well as resizing the array.
 * This class may be removed if JavaSoft provide some language construct
 * functionally equivalent to C/C++'s {@code realloc}.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/resources/XArray.java $
 * @version $Id: XArray.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 *
 * @todo Replace all {@code resize} methods by {@code Arrays.copyOf} when we will be allowed to
 *       compile for Java 6.
 */
public final class XArray {
    /**
     * All object constructions of this class are forbidden.
     */
    private XArray() {
    }

    /**
     * Returns an array containing the same elements as the given {@code array} but with the
     * specified {@code length}, truncating or padding with {@code null} if necessary.
     * <ul>
     *   <li><p>If the given {@code length} is longer than the length of the given {@code array},
     *       then the returned array will contain all the elements of {@code array} at index
     *       <var>i</var> &lt; {@code array.length}. Elements at index <var>i</var> &gt;=
     *       {@code array.length} are initialized to {@code null}.</p></li>
     *
     *   <li><p>If the given {@code length} is shorter than the length of the given {@code array},
     *       then the returned array will contain only the elements of {@code array} at index
     *       <var>i</var> &lt; {@code length}. Remainding elements are not copied.</p></li>
     *
     *   <li><p>If the given {@code length} is equals to the length of the given {@code array},
     *       then {@code array} is returned unchanged. <strong>No copy</strong> is performed.
     *       This behavior is what make this method different than {@link Arrays#copyOf}.</p></li>
     *
     * @param  <T> The array elements.
     * @param  array  Array to copy.
     * @param  length Length of the desired array.
     * @return A new array of the requested length, or {@code array} if the original
     *         array already have the requested length.
     *
     * @see Arrays#copyOf(Object[],int)
     */
    private static <T> T doResize(final T array, final int length) {
        final int current = array == null ? 0 : Array.getLength(array);
        if (current != length) {
            @SuppressWarnings("unchecked")
            final T newArray = (T) Array.newInstance(array.getClass().getComponentType(), length);
            System.arraycopy(array, 0, newArray, 0, Math.min(current, length));
            return newArray;
        } else {
            return array;
        }
    }

    /**
     * Returns an array containing the same elements as the given {@code array} but with the
     * specified {@code length}, truncating or padding with {@code null} if necessary.
     * <ul>
     *   <li><p>If the given {@code length} is longer than the length of the given {@code array},
     *       then the returned array will contain all the elements of {@code array} at index
     *       <var>i</var> &lt; {@code array.length}. Elements at index <var>i</var> &gt;=
     *       {@code array.length} are initialized to {@code null}.</p></li>
     *
     *   <li><p>If the given {@code length} is shorter than the length of the given {@code array},
     *       then the returned array will contain only the elements of {@code array} at index
     *       <var>i</var> &lt; {@code length}. Remainding elements are not copied.</p></li>
     *
     *   <li><p>If the given {@code length} is equals to the length of the given {@code array},
     *       then {@code array} is returned unchanged. <strong>No copy</strong> is performed.
     *       This behavior is what make this method different than {@link Arrays#copyOf}.</p></li>
     *
     * @param  <E> The array elements.
     * @param  array  Array to copy.
     * @param  length Length of the desired array.
     * @return A new array of the requested length, or {@code array} if the original
     *         array already have the requested length.
     *
     * @see Arrays#copyOf(Object[],int)
     */
    public static <E> E[] resize(final E[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns an array containing the same elements as the given {@code array} but
     * specified {@code length}, truncating or padding with zeros if necessary.
     *
     * @param  array  Array to copy.
     * @param  length Length of the desired array.
     * @return A new array of the requested length, or {@code array} if the original
     *         array already have the requested length.
     */
    public static int[] resize(final int[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Removes elements from the middle of an array.
     *
     * @param <T>     The type of array elements.
     * @param array   Array from which to remove elements.
     * @param index   Index of the first element to remove from the given {@code array}.
     * @param length  Number of elements to remove.
     * @return        Array with the same elements than the given {@code array} except for the
     *                removed elements, or {@code array} if {@code length} is 0.
     */
    private static <T> T doRemove(final T array, final int index, final int length) {
        if (length == 0) {
            return array;
        }
        int arrayLength = Array.getLength(array);
        @SuppressWarnings("unchecked")
        final T newArray = (T) Array.newInstance(array.getClass().getComponentType(), arrayLength -= length);
        System.arraycopy(array, 0,            newArray, 0,                 index);
        System.arraycopy(array, index+length, newArray, index, arrayLength-index);
        return newArray;
    }

    /**
     * Removes elements from the middle of an array.
     *
     * @param <E>     The type of array elements.
     * @param array   Array from which to remove elements.
     * @param index   Index of the first element to remove from the given {@code array}.
     * @param length  Number of elements to remove.
     * @return        Array with the same elements than the given {@code array} except for the
     *                removed elements, or {@code array} if {@code length} is 0.
     */
    public static <E> E[] remove(final E[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Inserts spaces into the middle of an array. These "spaces" will be made up of elements
     * initialized to {@code null}.
     *
     * @param array   Array in which to insert spaces.
     * @param index   Index where the first space should be inserted. All {@code array} elements
     *                having an index equal to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Array containing the {@code array} elements with the additional space
     *                inserted, or {@code array} if {@code length} is 0.
     */
    private static <T> T doInsert(final T array, final int index, final int length) {
        if (length == 0) {
            return array;
        }
        final int arrayLength = Array.getLength(array);
        @SuppressWarnings("unchecked")
        final T newArray = (T) Array.newInstance(array.getClass().getComponentType(), arrayLength + length);
        System.arraycopy(array, 0,     newArray, 0,            index            );
        System.arraycopy(array, index, newArray, index+length, arrayLength-index);
        return newArray;
    }

    /**
     * Inserts spaces into the middle of an array. These "spaces" will be made up of elements
     * initialized to {@code null}.
     *
     * @param array   Array in which to insert spaces.
     * @param index   Index where the first space should be inserted. All {@code array} elements
     *                having an index equal to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Array containing the {@code array} elements with the additional space
     *                inserted, or {@code array} if {@code length} is 0.
     */
    public static <E> E[] insert(final E[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }
}
