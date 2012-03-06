/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.util;

/**
 * A List of int's; as full an implementation of the java.util.List
 * interface as possible, with an eye toward minimal creation of
 * objects
 *
 * the mimicry of List is as follows:
 * <ul>
 * <li> if possible, operations designated 'optional' in the List
 *      interface are attempted
 * <li> wherever the List interface refers to an Object, substitute
 *      int
 * <li> wherever the List interface refers to a Collection or List,
 *      substitute IntList
 * </ul>
 *
 * the mimicry is not perfect, however:
 * <ul>
 * <li> operations involving Iterators or ListIterators are not
 *      supported
 * <li> remove(Object) becomes removeValue to distinguish it from
 *      remove(int index)
 * <li> subList is not supported
 * </ul>
 *
 * @author Marc Johnson
 */
public class IntList
{
    private int[]            _array;
    private int              _limit;
    private int              fillval = 0;
    private static final int _default_size = 128;

    /**
     * create an IntList of default size
     */

    public IntList()
    {
        this(_default_size);
    }

    public IntList(final int initialCapacity)
    {
        this(initialCapacity,0);
    }


    /**
     * create an IntList with a predefined initial size
     *
     * @param initialCapacity the size for the internal array
     */

    public IntList(final int initialCapacity, int fillvalue)
    {
        _array = new int[ initialCapacity ];
        if (fillval != 0) {
            fillval = fillvalue;
            fillArray(fillval, _array, 0);
        }
        _limit = 0;
    }

    private void fillArray(int val, int[] array, int index) {
      for (int k = index; k < array.length; k++) {
        array[k] = val;
      }
    }


    /**
     * Appends the specified element to the end of this list
     *
     * @param value element to be appended to this list.
     *
     * @return true (as per the general contract of the Collection.add
     *         method).
     */

    public boolean add(final int value)
    {
        if (_limit == _array.length)
        {
            growArray(_limit * 2);
        }
        _array[ _limit++ ] = value;
        return true;
    }

    /**
     * Appends all of the elements in the specified collection to the
     * end of this list, in the order that they are returned by the
     * specified collection's iterator.  The behavior of this
     * operation is unspecified if the specified collection is
     * modified while the operation is in progress.  (Note that this
     * will occur if the specified collection is this list, and it's
     * nonempty.)
     *
     * @param c collection whose elements are to be added to this
     *          list.
     *
     * @return true if this list changed as a result of the call.
     */

    public boolean addAll(final IntList c)
    {
        if (c._limit != 0)
        {
            if ((_limit + c._limit) > _array.length)
            {
                growArray(_limit + c._limit);
            }
            System.arraycopy(c._array, 0, _array, _limit, c._limit);
            _limit += c._limit;
        }
        return true;
    }



    /**
     * Compares the specified object with this list for equality.
     * Returns true if and only if the specified object is also a
     * list, both lists have the same size, and all corresponding
     * pairs of elements in the two lists are equal.  (Two elements e1
     * and e2 are equal if e1 == e2.)  In other words, two lists are
     * defined to be equal if they contain the same elements in the
     * same order.  This definition ensures that the equals method
     * works properly across different implementations of the List
     * interface.
     *
     * @param o the object to be compared for equality with this list.
     *
     * @return true if the specified object is equal to this list.
     */

    public boolean equals(final Object o)
    {
        boolean rval = this == o;

        if (!rval && (o != null) && (o.getClass() == this.getClass()))
        {
            IntList other = ( IntList ) o;

            if (other._limit == _limit)
            {

                // assume match
                rval = true;
                for (int j = 0; rval && (j < _limit); j++)
                {
                    rval = _array[ j ] == other._array[ j ];
                }
            }
        }
        return rval;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     *
     * @return the element at the specified position in this list.
     *
     * @exception IndexOutOfBoundsException if the index is out of
     *            range (index < 0 || index >= size()).
     */

    public int get(final int index)
    {
        if (index >= _limit)
        {
            throw new IndexOutOfBoundsException();
        }
        return _array[ index ];
    }

    /**
     * Returns the hash code value for this list.  The hash code of a
     * list is defined to be the result of the following calculation:
     *
     * <code>
     * hashCode = 1;
     * Iterator i = list.iterator();
     * while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     * }
     * </code>
     *
     * This ensures that list1.equals(list2) implies that
     * list1.hashCode()==list2.hashCode() for any two lists, list1 and
     * list2, as required by the general contract of Object.hashCode.
     *
     * @return the hash code value for this list.
     */

    public int hashCode()
    {
        int hash = 0;

        for (int j = 0; j < _limit; j++)
        {
            hash = (31 * hash) + _array[ j ];
        }
        return hash;
    }

    /**
     * Returns the number of elements in this list. If this list
     * contains more than Integer.MAX_VALUE elements, returns
     * Integer.MAX_VALUE.
     *
     * @return the number of elements in this IntList
     */

    public int size()
    {
        return _limit;
    }



    private void growArray(final int new_size)
    {
        int   size      = (new_size == _array.length) ? new_size + 1
                                                      : new_size;
        int[] new_array = new int[ size ];

        if (fillval != 0) {
          fillArray(fillval, new_array, _array.length);
        }

        System.arraycopy(_array, 0, new_array, 0, _limit);
        _array = new_array;
    }
}   // end public class IntList

