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

import org.apache.poi.ss.SpreadsheetVersion;

public class AreaReference {

    /** The character (:) that separates the two cell references in a multi-cell area reference */
    private static final char CELL_DELIMITER = ':';
    
    private final CellReference _firstCell;
    private final CellReference _lastCell;
    private final boolean _isSingleCell;
    
    /**
     * Creates an area ref from a pair of Cell References.
     */
    public AreaReference(CellReference topLeft, CellReference botRight) {
        boolean swapRows = topLeft.getRow() > botRight.getRow();
        boolean swapCols = topLeft.getCol() > botRight.getCol();
        if (swapRows || swapCols) {
            int firstRow; 
            int lastRow; 
            int firstColumn; 
            int lastColumn;
            boolean firstRowAbs; 
            boolean lastRowAbs; 
            boolean firstColAbs;
            boolean lastColAbs;   
            if (swapRows) {
                firstRow = botRight.getRow();
                firstRowAbs = botRight.isRowAbsolute();
                lastRow = topLeft.getRow();
                lastRowAbs = topLeft.isRowAbsolute();
            } else {
                firstRow = topLeft.getRow();
                firstRowAbs = topLeft.isRowAbsolute();
                lastRow = botRight.getRow();
                lastRowAbs = botRight.isRowAbsolute();
            }
            if (swapCols) {
                firstColumn = botRight.getCol();
                firstColAbs = botRight.isColAbsolute();
                lastColumn = topLeft.getCol();
                lastColAbs = topLeft.isColAbsolute();
            } else {
                firstColumn = topLeft.getCol();
                firstColAbs = topLeft.isColAbsolute();
                lastColumn = botRight.getCol();
                lastColAbs = botRight.isColAbsolute();
            }
            _firstCell = new CellReference(firstRow, firstColumn, firstRowAbs, firstColAbs);
            _lastCell = new CellReference(lastRow, lastColumn, lastRowAbs, lastColAbs);
        } else {
            _firstCell = topLeft;
            _lastCell = botRight;
        }
        _isSingleCell = false;
    }


    /**
     * Is the reference for a whole-column reference,
     *  such as C:C or D:G ?
     */
    public static boolean isWholeColumnReference(CellReference topLeft, CellReference botRight) {
        // These are represented as something like
        //   C$1:C$65535 or D$1:F$0
        // i.e. absolute from 1st row to 0th one
        if(topLeft.getRow() == 0 && topLeft.isRowAbsolute() &&
            botRight.getRow() == SpreadsheetVersion.EXCEL97.getLastRowIndex() && botRight.isRowAbsolute()) {
            return true;
        }
        return false;
    }
    public boolean isWholeColumnReference() {
        return isWholeColumnReference(_firstCell, _lastCell);
    }
    
    /**
     *  Example return values:
     *    <table border="0" cellpadding="1" cellspacing="0" summary="Example return values">
     *      <tr><th align='left'>Result</th><th align='left'>Comment</th></tr>
     *      <tr><td>A1:A1</td><td>Single cell area reference without sheet</td></tr>
     *      <tr><td>A1:$C$1</td><td>Multi-cell area reference without sheet</td></tr>
     *      <tr><td>Sheet1!A$1:B4</td><td>Standard sheet name</td></tr>
     *      <tr><td>'O''Brien''s Sales'!B5:C6'&nbsp;</td><td>Sheet name with special characters</td></tr>
     *    </table>
     * @return the text representation of this area reference as it would appear in a formula.
     */
    public String formatAsString() {
        // Special handling for whole-column references
        if(isWholeColumnReference()) {
            return
                CellReference.convertNumToColString(_firstCell.getCol())
                + ":" +
                CellReference.convertNumToColString(_lastCell.getCol());
        }
        
        StringBuffer sb = new StringBuffer(32);
        sb.append(_firstCell.formatAsString());
        if(!_isSingleCell) {
            sb.append(CELL_DELIMITER);
            if(_lastCell.getSheetName() == null) {
                sb.append(_lastCell.formatAsString());
            } else {
                // don't want to include the sheet name twice
                _lastCell.appendCellReference(sb);
            }
        }
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(formatAsString());
        sb.append("]");
        return sb.toString();
    }
}
