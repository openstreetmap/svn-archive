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

import java.util.Iterator;

/**
 * High level representation of a Excel worksheet.
 *
 * <p>
 * Sheets are the central structures within a workbook, and are where a user does most of his spreadsheet work.
 * The most common type of sheet is the worksheet, which is represented as a grid of cells. Worksheet cells can
 * contain text, numbers, dates, and formulas. Cells can also be formatted.
 * </p>
 */
public interface Sheet extends Iterable<Row> {

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return high level Row object representing a row in the sheet
     * @see #removeRow(Row)
     */
    Row createRow(int rownum);

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rownum  row to get (0-based)
     * @return Row representing the rownumber or null if its not defined on the sheet
     */
    Row getRow(int rownum);


    /**
     * Gets the first row on the sheet
     *
     * @return the number of the first logical row on the sheet (0-based)
     */
    int getFirstRowNum();

    /**
     * Gets the last row on the sheet
     *
     * @return last row contained n this sheet (0-based)
     */
    int getLastRowNum();

    /**
     *  Returns an iterator of the physical rows
     *
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     */
    Iterator<Row> rowIterator();


    /**
     * Remove a Array Formula from this sheet.  All cells contained in the Array Formula range are removed as well
     *
     * @param cell   any cell within Array Formula range
     * @return the {@link CellRange} of cells affected by this change
     */
    CellRange<? extends Cell> removeArrayFormula(Cell cell);
    
    
}
