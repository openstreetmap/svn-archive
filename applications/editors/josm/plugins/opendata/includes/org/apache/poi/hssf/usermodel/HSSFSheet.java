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

package org.apache.poi.hssf.usermodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.SSCellRange;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * High level representation of a worksheet.
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Libin Roman (romal at vistaportal.com)
 * @author  Shawn Laubach (slaubach at apache dot org) (Just a little)
 * @author  Jean-Pierre Paris (jean-pierre.paris at m4x dot org) (Just a little, too)
 * @author  Yegor Kozlov (yegor at apache.org) (Autosizing columns)
 * @author  Josh Micich
 * @author  Petr Udalau(Petr.Udalau at exigenservices.com) - set/remove array formulas
 */
public final class HSSFSheet implements org.apache.poi.ss.usermodel.Sheet {
    private static final POILogger log = POILogFactory.getLogger(HSSFSheet.class);
    private static final int DEBUG = POILogger.DEBUG;

    /**
     * reference to the low level {@link InternalSheet} object
     */
    private final InternalSheet _sheet;
    /** stores rows by zero-based row number */
    private final TreeMap<Integer, HSSFRow> _rows;
    protected final HSSFWorkbook _workbook;
    private int _firstrow;
    private int _lastrow;


    /**
     * Creates an HSSFSheet representing the given Sheet object.  Should only be
     * called by HSSFWorkbook when reading in an exisiting file.
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @param sheet - lowlevel Sheet object this sheet will represent
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */
    protected HSSFSheet(HSSFWorkbook workbook, InternalSheet sheet) {
        this._sheet = sheet;
        _rows = new TreeMap<Integer, HSSFRow>();
        this._workbook = workbook;
        setPropertiesFromSheet(sheet);
    }

    /**
     * used internally to set the properties given a Sheet object
     */
    private void setPropertiesFromSheet(InternalSheet sheet) {

        RowRecord row = sheet.getNextRow();
        boolean rowRecordsAlreadyPresent = row!=null;

        while (row != null) {
            createRowFromRecord(row);

            row = sheet.getNextRow();
        }

        CellValueRecordInterface[] cvals = sheet.getValueRecords();
        long timestart = System.currentTimeMillis();

        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "Time at start of cell creating in HSSF sheet = ",
                Long.valueOf(timestart));
        HSSFRow lastrow = null;

        // Add every cell to its row
        for (int i = 0; i < cvals.length; i++) {
            CellValueRecordInterface cval = cvals[i];

            long cellstart = System.currentTimeMillis();
            HSSFRow hrow = lastrow;

            if (hrow == null || hrow.getRowNum() != cval.getRow()) {
                hrow = getRow( cval.getRow() );
                lastrow = hrow;
                if (hrow == null) {
                    // Some tools (like Perl module Spreadsheet::WriteExcel - bug 41187) skip the RowRecords
                    // Excel, OpenOffice.org and GoogleDocs are all OK with this, so POI should be too.
                    if (rowRecordsAlreadyPresent) {
                        // if at least one row record is present, all should be present.
                        throw new RuntimeException("Unexpected missing row when some rows already present");
                    }
                    // create the row record on the fly now.
                    RowRecord rowRec = new RowRecord(cval.getRow());
                    sheet.addRow(rowRec);
                    hrow = createRowFromRecord(rowRec);
                }
            }
            if (log.check( POILogger.DEBUG ))
                log.log( DEBUG, "record id = " + Integer.toHexString( ( (Record) cval ).getSid() ) );
            hrow.createCellFromRecord( cval );
            if (log.check( POILogger.DEBUG ))
                log.log( DEBUG, "record took ",
                    Long.valueOf( System.currentTimeMillis() - cellstart ) );

        }
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "total sheet cell creation took ",
                Long.valueOf(System.currentTimeMillis() - timestart));
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return High level HSSFRow object representing a row in the sheet
     * @see org.apache.poi.hssf.usermodel.HSSFRow
     * @see #removeRow(org.apache.poi.ss.usermodel.Row)
     */
    public HSSFRow createRow(int rownum)
    {
        HSSFRow row = new HSSFRow(_workbook, this, rownum);

        addRow(row, true);
        return row;
    }

    /**
     * Used internally to create a high level Row object from a low level row object.
     * USed when reading an existing file
     * @param row  low level record to represent as a high level Row and add to sheet
     * @return HSSFRow high level representation
     */

    private HSSFRow createRowFromRecord(RowRecord row)
    {
        HSSFRow hrow = new HSSFRow(_workbook, this, row);

        addRow(hrow, false);
        return hrow;
    }


    /**
     * add a row to the sheet
     *
     * @param addLow whether to add the row to the low level model - false if its already there
     */

    private void addRow(HSSFRow row, boolean addLow)
    {
        _rows.put(Integer.valueOf(row.getRowNum()), row);
        if (addLow)
        {
            _sheet.addRow(row.getRowRecord());
        }
        boolean firstRow = _rows.size() == 1;
        if (row.getRowNum() > getLastRowNum() || firstRow)
        {
            _lastrow = row.getRowNum();
        }
        if (row.getRowNum() < getFirstRowNum() || firstRow)
        {
            _firstrow = row.getRowNum();
        }
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     * @param rowIndex  row to get
     * @return HSSFRow representing the row number or null if its not defined on the sheet
     */
    public HSSFRow getRow(int rowIndex) {
        return _rows.get(Integer.valueOf(rowIndex));
    }

    /**
     * Gets the first row on the sheet
     * @return the number of the first logical row on the sheet, zero based
     */
    public int getFirstRowNum() {
        return _firstrow;
    }

    /**
     * Gets the number last row on the sheet.
     * Owing to idiosyncrasies in the excel file
     *  format, if the result of calling this method
     *  is zero, you can't tell if that means there
     *  are zero rows on the sheet, or one at
     *  position zero. For that case, additionally
     *  call {@link #getPhysicalNumberOfRows()} to
     *  tell if there is a row at position zero
     *  or not.
     * @return the number of the last row contained in this sheet, zero based.
     */
    public int getLastRowNum() {
        return _lastrow;
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    public Iterator<Row> rowIterator() {
        @SuppressWarnings("unchecked") // can this clumsy generic syntax be improved?
        Iterator<Row> result = (Iterator<Row>)(Iterator<? extends Row>)_rows.values().iterator();
        return result;
    }
    /**
     * Alias for {@link #rowIterator()} to allow
     *  foreach loops
     */
    public Iterator<Row> iterator() {
        return rowIterator();
    }


    /**
     * used internally in the API to get the low level Sheet record represented by this
     * Object.
     * @return Sheet - low level representation of this HSSFSheet.
     */
    InternalSheet getSheet() {
        return _sheet;
    }

    /**
     * Also creates cells if they don't exist
     */
    private CellRange<HSSFCell> getCellRange(CellRangeAddress range) {
        int firstRow = range.getFirstRow();
        int firstColumn = range.getFirstColumn();
        int lastRow = range.getLastRow();
        int lastColumn = range.getLastColumn();
        int height = lastRow - firstRow + 1;
        int width = lastColumn - firstColumn + 1;
        List<HSSFCell> temp = new ArrayList<HSSFCell>(height*width);
        for (int rowIn = firstRow; rowIn <= lastRow; rowIn++) {
            for (int colIn = firstColumn; colIn <= lastColumn; colIn++) {
                HSSFRow row = getRow(rowIn);
                if (row == null) {
                    row = createRow(rowIn);
                }
                HSSFCell cell = row.getCell(colIn);
                if (cell == null) {
                    cell = row.createCell(colIn);
                }
                temp.add(cell);
            }
        }
        return SSCellRange.create(firstRow, firstColumn, height, width, temp, HSSFCell.class);
    }

    public CellRange<HSSFCell> removeArrayFormula(Cell cell) {
        if (cell.getSheet() != this) {
            throw new IllegalArgumentException("Specified cell does not belong to this sheet.");
        }
        CellValueRecordInterface rec = ((HSSFCell) cell).getCellValueRecord();
        if (!(rec instanceof FormulaRecordAggregate)) {
            String ref = new CellReference(cell).formatAsString();
            throw new IllegalArgumentException("Cell " + ref + " is not part of an array formula.");
        }
        FormulaRecordAggregate fra = (FormulaRecordAggregate) rec;
        CellRangeAddress range = fra.removeArrayFormula(cell.getRowIndex(), cell.getColumnIndex());

        CellRange<HSSFCell> result = getCellRange(range);
        // clear all cells in the range
        for (Cell c : result) {
            c.setCellType(Cell.CELL_TYPE_BLANK);
        }
        return result;
    }
    

}
