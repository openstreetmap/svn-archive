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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * High level representation of a row of a spreadsheet.
 *
 * Only rows that have cells should be added to a Sheet.
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class HSSFRow implements Row {

    // used for collections
    public final static int INITIAL_CAPACITY = 5;

    private int rowNum;
    private HSSFCell[] cells=new HSSFCell[INITIAL_CAPACITY];

    /**
     * reference to low level representation
     */
    private RowRecord row;

    /**
     * reference to containing low level Workbook
     */
    private HSSFWorkbook book;

    /**
     * reference to containing Sheet
     */
    private HSSFSheet sheet;

    /**
     * Creates new HSSFRow from scratch. Only HSSFSheet should do this.
     *
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param rowNum the row number of this row (0 based)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#createRow(int)
     */
    HSSFRow(HSSFWorkbook book, HSSFSheet sheet, int rowNum) {
        this(book, sheet, new RowRecord(rowNum));
    }

    /**
     * Creates an HSSFRow from a low level RowRecord object.  Only HSSFSheet should do
     * this.  HSSFSheet uses this when an existing file is read in.
     *
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param record the low level api object this row should represent
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#createRow(int)
     */
    HSSFRow(HSSFWorkbook book, HSSFSheet sheet, RowRecord record) {
        this.book = book;
        this.sheet = sheet;
        row = record;
        setRowNum(record.getRowNumber());
        // Don't trust colIx boundaries as read by other apps
        // set the RowRecord empty for the moment
        record.setEmpty();
        // subsequent calls to createCellFromRecord() will update the colIx boundaries properly
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     *
     * @return HSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 255,
     *   the maximum number of columns supported by the Excel binary format (.xls)
     */
    public HSSFCell createCell(int column)
    {
        return this.createCell(column,HSSFCell.CELL_TYPE_BLANK);
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling setCellValue or setCellType.
     *
     * @param columnIndex - the column number this cell represents
     *
     * @return HSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 255,
     *   the maximum number of columns supported by the Excel binary format (.xls)
     */
    public HSSFCell createCell(int columnIndex, int type)
    {
        short shortCellNum = (short)columnIndex;
        if(columnIndex > 0x7FFF) {
            shortCellNum = (short)(0xffff - columnIndex);
        }

        HSSFCell cell = new HSSFCell(book, sheet, getRowNum(), shortCellNum, type);
        addCell(cell);
        sheet.getSheet().addValueRecord(getRowNum(), cell.getCellValueRecord());
        return cell;
    }

    /**
     * create a high level HSSFCell object from an existing low level record.  Should
     * only be called from HSSFSheet or HSSFRow itself.
     * @param cell low level cell to create the high level representation from
     * @return HSSFCell representing the low level record passed in
     */
    HSSFCell createCellFromRecord(CellValueRecordInterface cell) {
        HSSFCell hcell = new HSSFCell(book, sheet, cell);

        addCell(hcell);
        int colIx = cell.getColumn();
        if (row.isEmpty()) {
            row.setFirstCol(colIx);
            row.setLastCol(colIx + 1);
        } else {
            if (colIx < row.getFirstCol()) {
                row.setFirstCol(colIx);
            } else if (colIx > row.getLastCol()) {
                row.setLastCol(colIx + 1);
            } else {
                // added cell is within first and last cells
            }
        }
        // TODO - RowRecord column boundaries need to be updated for cell comments too
        return hcell;
    }

    /**
     * set the row number of this row.
     * @param rowIndex  the row number (0-based)
     * @throws IndexOutOfBoundsException if the row number is not within the range 0-65535.
     */
    public void setRowNum(int rowIndex) {
        int maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        if ((rowIndex < 0) || (rowIndex > maxrow)) {
          throw new IllegalArgumentException("Invalid row number (" + rowIndex
                  + ") outside allowable range (0.." + maxrow + ")");
        }
        rowNum = rowIndex;
        if (row != null) {
            row.setRowNumber(rowIndex);   // used only for KEY comparison (HSSFRow)
        }
    }

    /**
     * get row number this row represents
     * @return the row number (0 based)
     */
    public int getRowNum()
    {
        return rowNum;
    }

    /**
     * Returns the HSSFSheet this row belongs to
     *
     * @return the HSSFSheet that owns this row
     */
    public HSSFSheet getSheet()
    {
        return sheet;
    }


    /**
     * used internally to add a cell.
     */
    private void addCell(HSSFCell cell) {

        int column=cell.getColumnIndex();
        // re-allocate cells array as required.
        if(column>=cells.length) {
            HSSFCell[] oldCells=cells;
            int newSize=oldCells.length*2;
            if(newSize<column+1) {
                newSize=column+1;
            }
            cells=new HSSFCell[newSize];
            System.arraycopy(oldCells,0,cells,0,oldCells.length);
        }
        cells[column]=cell;

        // fix up firstCol and lastCol indexes
        if (row.isEmpty() || column < row.getFirstCol()) {
            row.setFirstCol((short)column);
        }

        if (row.isEmpty() || column >= row.getLastCol()) {
            row.setLastCol((short) (column+1)); // +1 -> for one past the last index
        }
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     *  0-based. If you ask for a cell that is not defined, then
     *  you get a null.
     * This is the basic call, with no policies applied
     *
     * @param cellIndex  0 based column number
     * @return HSSFCell representing that column or null if undefined.
     */
    private HSSFCell retrieveCell(int cellIndex) {
        if(cellIndex<0||cellIndex>=cells.length) {
            return null;
        }
        return cells[cellIndex];
    }


    /**
     * Get the hssfcell representing a given column (logical cell)
     *  0-based.  If you ask for a cell that is not defined then
     *  you get a null, unless you have set a different
     *  {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy} on the base workbook.
     *
     * @param cellnum  0 based column number
     * @return HSSFCell representing that column or null if undefined.
     */
    public HSSFCell getCell(int cellnum) {
        return getCell(cellnum, book.getMissingCellPolicy());
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     *  0-based.  If you ask for a cell that is not defined, then
     *  your supplied policy says what to do
     *
     * @param cellnum  0 based column number
     * @param policy Policy on blank / missing cells
     * @return representing that column or null if undefined + policy allows.
     */
    public HSSFCell getCell(int cellnum, MissingCellPolicy policy) {
        HSSFCell cell = retrieveCell(cellnum);
        if(policy == RETURN_NULL_AND_BLANK) {
            return cell;
        }
        if(policy == RETURN_BLANK_AS_NULL) {
            if(cell == null) return cell;
            if(cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
                return null;
            }
            return cell;
        }
        if(policy == CREATE_NULL_AS_BLANK) {
            if(cell == null) {
                return createCell(cellnum, HSSFCell.CELL_TYPE_BLANK);
            }
            return cell;
        }
        throw new IllegalArgumentException("Illegal policy " + policy + " (" + policy.id + ")");
    }

    /**
     * get the lowlevel RowRecord represented by this object - should only be called
     * by other parts of the high level API
     *
     * @return RowRecord this row represents
     */

    protected RowRecord getRowRecord()
    {
        return row;
    }

    /**
     * @return cell iterator of the physically defined cells.
     * Note that the 4th element might well not be cell 4, as the iterator
     *  will not return un-defined (null) cells.
     * Call getCellNum() on the returned cells to know which cell they are.
     * As this only ever works on physically defined cells,
     *  the {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy} has no effect.
     */
    public Iterator<Cell> cellIterator()
    {
      return new CellIterator();
    }
    /**
     * Alias for {@link #cellIterator} to allow
     *  foreach loops
     */
    public Iterator<Cell> iterator() {
       return cellIterator();
    }

    /**
     * An iterator over the (physical) cells in the row.
     */
    private class CellIterator implements Iterator<Cell> {
      int thisId=-1;
      int nextId=-1;

      public CellIterator()
      {
        findNext();
      }

      public boolean hasNext() {
        return nextId<cells.length;
      }

      public Cell next() {
          if (!hasNext())
              throw new NoSuchElementException("At last element");
        HSSFCell cell=cells[nextId];
        thisId=nextId;
        findNext();
        return cell;
      }

      public void remove() {
          if (thisId == -1)
              throw new IllegalStateException("remove() called before next()");
        cells[thisId]=null;
      }

      private void findNext()
      {
        int i=nextId+1;
        for(;i<cells.length;i++)
        {
          if(cells[i]!=null) break;
        }
        nextId=i;
      }

    }


    public boolean equals(Object obj)
    {
        if (!(obj instanceof HSSFRow))
        {
            return false;
        }
        HSSFRow loc = (HSSFRow) obj;

        if (this.getRowNum() == loc.getRowNum())
        {
            return true;
        }
        return false;
    }
}
