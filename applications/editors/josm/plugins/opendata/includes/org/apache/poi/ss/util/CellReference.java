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

import org.apache.poi.hssf.record.formula.SheetNameFormatter;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;

/**
 *
 * @author  Avik Sengupta
 * @author  Dennis Doubleday (patch to seperateRowColumns())
 */
public class CellReference {

	/** The character ($) that signifies a row or column value is absolute instead of relative */
	private static final char ABSOLUTE_REFERENCE_MARKER = '$';
	/** The character (!) that separates sheet names from cell references */
	private static final char SHEET_NAME_DELIMITER = '!';

	//private static final String BIFF8_LAST_COLUMN = SpreadsheetVersion.EXCEL97.getLastColumnName();
	//private static final int BIFF8_LAST_COLUMN_TEXT_LEN = BIFF8_LAST_COLUMN.length();
	//private static final String BIFF8_LAST_ROW = String.valueOf(SpreadsheetVersion.EXCEL97.getMaxRows());
	//private static final int BIFF8_LAST_ROW_TEXT_LEN = BIFF8_LAST_ROW.length();

	private final int _rowIndex;
	private final int _colIndex;
	private final String _sheetName;
	private final boolean _isRowAbs;
	private final boolean _isColAbs;


	public CellReference(int pRow, int pCol) {
		this(pRow, pCol, false, false);
	}
	public CellReference(int pRow, short pCol) {
		this(pRow, pCol & 0xFFFF, false, false);
	}

	public CellReference(Cell cell) {
		this(cell.getRowIndex(), cell.getColumnIndex(), false, false);
	}

	public CellReference(int pRow, int pCol, boolean pAbsRow, boolean pAbsCol) {
		this(null, pRow, pCol, pAbsRow, pAbsCol);
	}
	public CellReference(String pSheetName, int pRow, int pCol, boolean pAbsRow, boolean pAbsCol) {
		// TODO - "-1" is a special value being temporarily used for whole row and whole column area references.
		// so these checks are currently N.Q.R.
		if(pRow < -1) {
			throw new IllegalArgumentException("row index may not be negative");
		}
		if(pCol < -1) {
			throw new IllegalArgumentException("column index may not be negative");
		}
		_sheetName = pSheetName;
		_rowIndex=pRow;
		_colIndex=pCol;
		_isRowAbs = pAbsRow;
		_isColAbs=pAbsCol;
	}

	public int getRow(){return _rowIndex;}
	public short getCol(){return (short) _colIndex;}
	public boolean isRowAbsolute(){return _isRowAbs;}
	public boolean isColAbsolute(){return _isColAbs;}
	/**
	  * @return possibly <code>null</code> if this is a 2D reference.  Special characters are not
	  * escaped or delimited
	  */
	public String getSheetName(){
		return _sheetName;
	}

	/**
	 * Used to decide whether a name of the form "[A-Z]*[0-9]*" that appears in a formula can be
	 * interpreted as a cell reference.  Names of that form can be also used for sheets and/or
	 * named ranges, and in those circumstances, the question of whether the potential cell
	 * reference is valid (in range) becomes important.
	 * <p/>
	 * Note - that the maximum sheet size varies across Excel versions:
	 * <p/>
	 * <blockquote><table border="0" cellpadding="1" cellspacing="0"
	 *                 summary="Notable cases.">
	 *   <tr><th>Version&nbsp;&nbsp;</th><th>File Format&nbsp;&nbsp;</th>
	 *   	<th>Last Column&nbsp;&nbsp;</th><th>Last Row</th></tr>
	 *   <tr><td>97-2003</td><td>BIFF8</td><td>"IV" (2^8)</td><td>65536 (2^14)</td></tr>
	 *   <tr><td>2007</td><td>BIFF12</td><td>"XFD" (2^14)</td><td>1048576 (2^20)</td></tr>
	 * </table></blockquote>
	 * POI currently targets BIFF8 (Excel 97-2003), so the following behaviour can be observed for
	 * this method:
	 * <blockquote><table border="0" cellpadding="1" cellspacing="0"
	 *                 summary="Notable cases.">
	 *   <tr><th>Input&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 *       <th>Result&nbsp;</th></tr>
	 *   <tr><td>"A", "1"</td><td>true</td></tr>
	 *   <tr><td>"a", "111"</td><td>true</td></tr>
	 *   <tr><td>"A", "65536"</td><td>true</td></tr>
	 *   <tr><td>"A", "65537"</td><td>false</td></tr>
	 *   <tr><td>"iv", "1"</td><td>true</td></tr>
	 *   <tr><td>"IW", "1"</td><td>false</td></tr>
	 *   <tr><td>"AAA", "1"</td><td>false</td></tr>
	 *   <tr><td>"a", "111"</td><td>true</td></tr>
	 *   <tr><td>"Sheet", "1"</td><td>false</td></tr>
	 * </table></blockquote>
	 *
	 * @param colStr a string of only letter characters
	 * @param rowStr a string of only digit characters
	 * @return <code>true</code> if the row and col parameters are within range of a BIFF8 spreadsheet.
	 */
	public static boolean cellReferenceIsWithinRange(String colStr, String rowStr, SpreadsheetVersion ssVersion) {
		if (!isColumnWithnRange(colStr, ssVersion)) {
			return false;
		}
		return isRowWithnRange(rowStr, ssVersion);
	}

	public static boolean isColumnWithnRange(String colStr, SpreadsheetVersion ssVersion) {
		String lastCol = ssVersion.getLastColumnName();
		int lastColLength = lastCol.length();

		int numberOfLetters = colStr.length();
		if(numberOfLetters > lastColLength) {
			// "Sheet1" case etc
			return false; // that was easy
		}
		if(numberOfLetters == lastColLength) {
			if(colStr.toUpperCase().compareTo(lastCol) > 0) {
				return false;
			}
		} else {
			// apparent column name has less chars than max
			// no need to check range
		}
		return true;
	}

	public static boolean isRowWithnRange(String rowStr, SpreadsheetVersion ssVersion) {
		int rowNum = Integer.parseInt(rowStr);

		if (rowNum < 0) {
			throw new IllegalStateException("Invalid rowStr '" + rowStr + "'.");
		}
		if (rowNum == 0) {
			// execution gets here because caller does first pass of discriminating
			// potential cell references using a simplistic regex pattern.
			return false;
		}
		return rowNum <= ssVersion.getMaxRows();
	}

	/**
	 * Takes in a 0-based base-10 column and returns a ALPHA-26
	 *  representation.
	 * eg column #3 -> D
	 */
	public static String convertNumToColString(int col) {
		// Excel counts column A as the 1st column, we
		//  treat it as the 0th one
		int excelColNum = col + 1;

		String colRef = "";
		int colRemain = excelColNum;

		while(colRemain > 0) {
			int thisPart = colRemain % 26;
			if(thisPart == 0) { thisPart = 26; }
			colRemain = (colRemain - thisPart) / 26;

			// The letter A is at 65
			char colChar = (char)(thisPart+64);
			colRef = colChar + colRef;
		}

		return colRef;
	}

	/**
	 *  Example return values:
	 *	<table border="0" cellpadding="1" cellspacing="0" summary="Example return values">
	 *	  <tr><th align='left'>Result</th><th align='left'>Comment</th></tr>
	 *	  <tr><td>A1</td><td>Cell reference without sheet</td></tr>
	 *	  <tr><td>Sheet1!A1</td><td>Standard sheet name</td></tr>
	 *	  <tr><td>'O''Brien''s Sales'!A1'&nbsp;</td><td>Sheet name with special characters</td></tr>
	 *	</table>
	 * @return the text representation of this cell reference as it would appear in a formula.
	 */
	public String formatAsString() {
		StringBuffer sb = new StringBuffer(32);
		if(_sheetName != null) {
			SheetNameFormatter.appendFormat(sb, _sheetName);
			sb.append(SHEET_NAME_DELIMITER);
		}
		appendCellReference(sb);
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(formatAsString());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Appends cell reference with '$' markers for absolute values as required.
	 * Sheet name is not included.
	 */
	/* package */ void appendCellReference(StringBuffer sb) {
		if(_isColAbs) {
			sb.append(ABSOLUTE_REFERENCE_MARKER);
		}
		sb.append( convertNumToColString(_colIndex));
		if(_isRowAbs) {
			sb.append(ABSOLUTE_REFERENCE_MARKER);
		}
		sb.append(_rowIndex+1);
	}

	/**
	 * Checks whether this cell reference is equal to another object.
	 * <p>
	 *  Two cells references are assumed to be equal if their string representations
	 *  ({@link #formatAsString()}  are equal.
	 * </p>
	 */
	@Override
	public boolean equals(Object o){
		if(!(o instanceof CellReference)) {
			return false;
		}
		CellReference cr = (CellReference) o;
		return _rowIndex == cr._rowIndex
			&& _colIndex == cr._colIndex
			&& _isRowAbs == cr._isColAbs
			&& _isColAbs == cr._isColAbs;
	}
}
