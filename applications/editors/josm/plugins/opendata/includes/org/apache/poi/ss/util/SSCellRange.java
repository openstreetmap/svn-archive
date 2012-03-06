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

package org.apache.poi.ss.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.util.Internal;

/**
 * For POI internal use only
 *
 * @author Josh Micich
 */
@Internal
public final class SSCellRange<K extends Cell> implements CellRange<K> {

	private final K[] _flattenedArray;

	private SSCellRange(int firstRow, int firstColumn, int height, int width, K[] flattenedArray) {
		_flattenedArray = flattenedArray;
	}

	public static <B extends Cell> SSCellRange<B> create(int firstRow, int firstColumn, int height, int width, List<B> flattenedList, Class<B> cellClass) {
		int nItems = flattenedList.size();
		if (height * width != nItems) {
			throw new IllegalArgumentException("Array size mismatch.");
		}

		@SuppressWarnings("unchecked")
		B[] flattenedArray = (B[]) Array.newInstance(cellClass, nItems);
		flattenedList.toArray(flattenedArray);
		return new SSCellRange<B>(firstRow, firstColumn, height, width, flattenedArray);
	}

	public Iterator<K> iterator() {
		return new ArrayIterator<K>(_flattenedArray);
	}
	private static final class ArrayIterator<D> implements Iterator<D> {

		private final D[] _array;
		private int _index;

		public ArrayIterator(D[] array) {
			_array = array;
			_index = 0;
		}
		public boolean hasNext() {
			return _index < _array.length;
		}
		public D next() {
			if (_index >= _array.length) {
				throw new NoSuchElementException(String.valueOf(_index));
			}
			return _array[_index++];
		}

		public void remove() {
			throw new UnsupportedOperationException("Cannot remove cells from this CellRange.");
		}
	}
}
