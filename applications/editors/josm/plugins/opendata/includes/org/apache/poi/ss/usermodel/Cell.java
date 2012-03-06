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

package org.apache.poi.ss.usermodel;

import java.util.Date;

import org.apache.poi.ss.util.CellRangeAddress;

/**
 * High level representation of a cell in a row of a spreadsheet.
 * <p>
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot conatin numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells have the formula string, as well as
 * the formula result, which can be numeric or string.
 * </p>
 * <p>
 * Cells should have their number (0 based) before being added to a row.
 * </p>
 */
public interface Cell {

    /**
     * Numeric Cell type (0)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_NUMERIC = 0;

    /**
     * String Cell type (1)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_STRING = 1;

    /**
     * Formula Cell type (2)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_FORMULA = 2;

    /**
     * Blank Cell type (3)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_BLANK = 3;

    /**
     * Boolean Cell type (4)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_BOOLEAN = 4;

    /**
     * Error Cell type (5)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_ERROR = 5;

    /**
     * Returns column index of this cell
     *
     * @return zero-based column index of a column in a sheet.
     */
    int getColumnIndex();

    /**
     * Returns row index of a row in the sheet that contains this cell
     *
     * @return zero-based row index of a row in the sheet that contains this cell
     */
    int getRowIndex();

    /**
     * Returns the sheet this cell belongs to
     *
     * @return the sheet this cell belongs to
     */
    Sheet getSheet();

    /**
     * Returns the Row this cell belongs to
     *
     * @return the Row that owns this cell
     */
     Row getRow();

    /**
     * Set the cells type (numeric, formula or string)
     *
     * @throws IllegalArgumentException if the specified cell type is invalid
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BLANK
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */
    void setCellType(int cellType);

    /**
     * Return the cell type.
     *
     * @return the cell type
     * @see Cell#CELL_TYPE_BLANK
     * @see Cell#CELL_TYPE_NUMERIC
     * @see Cell#CELL_TYPE_STRING
     * @see Cell#CELL_TYPE_FORMULA
     * @see Cell#CELL_TYPE_BOOLEAN
     * @see Cell#CELL_TYPE_ERROR
     */
    int getCellType();

    /**
     * Return a formula for the cell, for example, <code>SUM(C4:E4)</code>
     *
     * @return a formula for the cell
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is not CELL_TYPE_FORMULA
     */
    String getCellFormula();

    /**
     * Get the value of the cell as a number.
     * <p>
     * For strings we throw an exception. For blank cells we return a 0.
     * For formulas or error cells we return the precalculated value;
     * </p>
     * @return the value of the cell as a number
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CELL_TYPE_STRING
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for turning this number into a string similar to that which Excel would render this number as.
     */
    double getNumericCellValue();

    /**
     * Get the value of the cell as a date.
     * <p>
     * For strings we throw an exception. For blank cells we return a null.
     * </p>
     * @return the value of the cell as a date
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CELL_TYPE_STRING
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for formatting  this date into a string similar to how excel does.
     */
    Date getDateCellValue();

    /**
     * Get the value of the cell as a XSSFRichTextString
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formula cells we return the pre-calculated value if a string, otherwise an exception.
     * </p>
     * @return the value of the cell as a XSSFRichTextString
     */
    RichTextString getRichStringCellValue();

    /**
     * Get the value of the cell as a string
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception.
     * </p>
     * @return the value of the cell as a string
     */
    String getStringCellValue();

    /**
     * Get the value of the cell as a boolean.
     * <p>
     * For strings, numbers, and errors, we throw an exception. For blank cells we return a false.
     * </p>
     * @return the value of the cell as a boolean
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()}
     *   is not CELL_TYPE_BOOLEAN, CELL_TYPE_BLANK or CELL_TYPE_FORMULA
     */
    boolean getBooleanCellValue();

    /**
     * Return the cell's style.
     *
     * @return the cell's style. Always not-null. Default cell style has zero index and can be obtained as
     * <code>workbook.getCellStyleAt(0)</code>
     * @see Workbook#getCellStyleAt(short)
     */
    CellStyle getCellStyle();

    /**
     * Only valid for array formula cells
     *
     * @return range of the array formula group that the cell belongs to.
     */
    CellRangeAddress getArrayFormulaRange();

    /**
     * @return <code>true</code> if this cell is part of group of cells having a common array formula.
     */
    boolean isPartOfArrayFormulaGroup();
}
