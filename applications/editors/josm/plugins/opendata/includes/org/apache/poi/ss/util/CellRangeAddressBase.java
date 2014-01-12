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



/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p/>
 *
 * Common subclass of 8-bit and 16-bit versions
 *
 * @author Josh Micich
 */
public abstract class CellRangeAddressBase {

	private int _firstRow;
	private int _firstCol;
	private int _lastRow;
	private int _lastCol;

	protected CellRangeAddressBase(int firstRow, int lastRow, int firstCol, int lastCol) {
		_firstRow = firstRow;
		_lastRow = lastRow;
		_firstCol = firstCol;
		_lastCol = lastCol;
	}

	/**
	 * @return column number for the upper left hand corner
	 */
	public final int getFirstColumn() {
		return _firstCol;
	}

	/**
	 * @return row number for the upper left hand corner
	 */
	public final int getFirstRow() {
		return _firstRow;
	}

	/**
	 * @return column number for the lower right hand corner
	 */
	public final int getLastColumn() {
		return _lastCol;
	}

	/**
	 * @return row number for the lower right hand corner
	 */
	public final int getLastRow() {
		return _lastRow;
	}

	/**
	 * @return the size of the range (number of cells in the area).
	 */
	public int getNumberOfCells() {
		return (_lastRow - _firstRow + 1) * (_lastCol - _firstCol + 1);
	}

	public final String toString() {
		CellReference crA = new CellReference(_firstRow, _firstCol);
		CellReference crB = new CellReference(_lastRow, _lastCol);
		return getClass().getName() + " [" + crA.formatAsString() + ":" + crB.formatAsString() +"]";
	}
}
