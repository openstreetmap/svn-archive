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

package org.apache.poi.hssf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.CalcCountRecord;
import org.apache.poi.hssf.record.CalcModeRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.record.DeltaRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FeatHdrRecord;
import org.apache.poi.hssf.record.FeatRecord;
import org.apache.poi.hssf.record.GridsetRecord;
import org.apache.poi.hssf.record.GutsRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.IterationRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RefModeRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SaveRecalcRecord;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.UncalcedRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.aggregates.ColumnInfoRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.DataValidityTable;
import org.apache.poi.hssf.record.aggregates.MergedCellsTable;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Low level model implementation of a Sheet (one workbook contains many sheets)
 * This file contains the low level binary records starting at the sheets BOF and
 * ending with the sheets EOF.  Use HSSFSheet for a high level representation.
 * <P>
 * The structures of the highlevel API use references to this to perform most of their
 * operations.  Its probably unwise to use these low level structures directly unless you
 * really know what you're doing.  I recommend you read the Microsoft Excel 97 Developer's
 * Kit (Microsoft Press) and the documentation at http://sc.openoffice.org/excelfileformat.pdf
 * before even attempting to use this.
 * <P>
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Shawn Laubach (slaubach at apache dot org) Gridlines, Headers, Footers, PrintSetup, and Setting Default Column Styles
 * @author Jason Height (jheight at chariot dot net dot au) Clone support. DBCell & Index Record writing support
 * @author  Brian Sanders (kestrel at burdell dot org) Active Cell support
 * @author  Jean-Pierre Paris (jean-pierre.paris at m4x dot org) (Just a little)
 *
 * @see org.apache.poi.hssf.model.InternalWorkbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 */
@Internal
public final class InternalSheet {

    private static POILogger            log              = POILogFactory.getLogger(InternalSheet.class);

    private List<RecordBase>             _records;
    protected GridsetRecord              gridset           =     null;
    private   GutsRecord                 _gutsRecord;
    protected WindowTwoRecord            windowTwo         =     null;
    protected SelectionRecord            _selection         =     null;
    /** java object always present, but if empty no BIFF records are written */
    private final MergedCellsTable       _mergedCellsTable;
    /** always present in this POI object, not always written to Excel file */
    /*package*/ColumnInfoRecordsAggregate _columnInfos;
    /** the DimensionsRecord is always present */
    private DimensionsRecord             _dimensions;
    /** always present */
    protected final RowRecordsAggregate  _rowsAggregate;
    private   DataValidityTable          _dataValidityTable=     null;
    private   Iterator<RowRecord>        rowRecIterator    =     null;

    /**
     * read support  (offset used as starting point for search) for low level
     * API.  Pass in an array of Record objects, the sheet number (0 based) and
     * a record offset (should be the location of the sheets BOF record).  A Sheet
     * object is constructed and passed back with all of its initialization set
     * to the passed in records and references to those records held. This function
     * is normally called via Workbook.
     *
     * @param rs the stream to read records from
     *
     * @return Sheet object with all values set to those read from the file
     *
     * @see org.apache.poi.hssf.model.InternalWorkbook
     * @see org.apache.poi.hssf.record.Record
     */
    public static InternalSheet createSheet(RecordStream rs) {
        return new InternalSheet(rs);
    }
    private InternalSheet(RecordStream rs) {
        _mergedCellsTable = new MergedCellsTable();
        RowRecordsAggregate rra = null;

        List<RecordBase> records = new ArrayList<RecordBase>(128);
        _records = records; // needed here due to calls to findFirstRecordLocBySid before we're done
        int dimsloc = -1;

        if (rs.peekNextSid() != BOFRecord.sid) {
            throw new RuntimeException("BOF record expected");
        }
        BOFRecord bof = (BOFRecord) rs.getNext();
        if (bof.getType() != BOFRecord.TYPE_WORKSHEET) {
            // TODO - fix junit tests throw new RuntimeException("Bad BOF record type");
        }
        records.add(bof);
        while (rs.hasNext()) {
            int recSid = rs.peekNextSid();

            if (recSid == ColumnInfoRecord.sid) {
                _columnInfos = new ColumnInfoRecordsAggregate(rs);
                records.add(_columnInfos);
                continue;
            }
            if ( recSid == DVALRecord.sid) {
                _dataValidityTable = new DataValidityTable(rs);
                records.add(_dataValidityTable);
                continue;
            }

            if (RecordOrderer.isRowBlockRecord(recSid)) {
                //only add the aggregate once
                if (rra != null) {
                    throw new RuntimeException("row/cell records found in the wrong place");
                }
                RowBlocksReader rbr = new RowBlocksReader(rs);
                _mergedCellsTable.addRecords(rbr.getLooseMergedCells());
                rra = new RowRecordsAggregate(rbr.getPlainRecordStream(), rbr.getSharedFormulaManager());
                records.add(rra); //only add the aggregate once
                continue;
            }

            if (recSid == MergeCellsRecord.sid) {
                // when the MergedCellsTable is found in the right place, we expect those records to be contiguous
                _mergedCellsTable.read(rs);
                continue;
            }

            if (recSid == BOFRecord.sid) {
                continue;
            }

            Record rec = rs.getNext();
            if ( recSid == IndexRecord.sid ) {
                // ignore INDEX record because it is only needed by Excel,
                // and POI always re-calculates its contents
                continue;
            }


            if (recSid == UncalcedRecord.sid) {
                // don't add UncalcedRecord to the list
                continue;
            }

            if (recSid == FeatRecord.sid ||
            		recSid == FeatHdrRecord.sid) {
                records.add(rec);
                continue;
            }
            
            if (recSid == EOFRecord.sid) {
                records.add(rec);
                break;
            }

            if (recSid == DimensionsRecord.sid)
            {
                // Make a columns aggregate if one hasn't ready been created.
                if (_columnInfos == null)
                {
                    _columnInfos = new ColumnInfoRecordsAggregate();
                    records.add(_columnInfos);
                }

                _dimensions    = ( DimensionsRecord ) rec;
                dimsloc = records.size();
            }
            else if ( recSid == GridsetRecord.sid )
            {
                gridset = (GridsetRecord) rec;
            }
            else if ( recSid == SelectionRecord.sid )
            {
                _selection = (SelectionRecord) rec;
            }
            else if ( recSid == WindowTwoRecord.sid )
            {
                windowTwo = (WindowTwoRecord) rec;
            }
            else if ( recSid == GutsRecord.sid )
            {
                _gutsRecord = (GutsRecord) rec;
            }

            records.add(rec);
        }
        if (windowTwo == null) {
            throw new RuntimeException("WINDOW2 was not found");
        }
        if (_dimensions == null) {
            // Excel seems to always write the DIMENSION record, but tolerates when it is not present
            // in all cases Excel (2007) adds the missing DIMENSION record
            if (rra == null) {
                // bug 46206 alludes to files which skip the DIMENSION record
                // when there are no row/cell records.
                // Not clear which application wrote these files.
                rra = new RowRecordsAggregate();
            } else {
                log.log(POILogger.WARN, "DIMENSION record not found even though row/cells present");
                // Not sure if any tools write files like this, but Excel reads them OK
            }
            dimsloc = findFirstRecordLocBySid(WindowTwoRecord.sid);
            _dimensions = rra.createDimensions();
            records.add(dimsloc, _dimensions);
        }
        if (rra == null) {
            rra = new RowRecordsAggregate();
            records.add(dimsloc + 1, rra);
        }
        _rowsAggregate = rra;
        // put merged cells table in the right place (regardless of where the first MergedCellsRecord was found */
        RecordOrderer.addNewSheetRecord(records, _mergedCellsTable);
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "sheet createSheet (existing file) exited");
    }

    private InternalSheet() {
        _mergedCellsTable = new MergedCellsTable();
        List<RecordBase> records = new ArrayList<RecordBase>(32);

        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet createsheet from scratch called");

        records.add(createBOF());

        records.add(createCalcMode());
        records.add(createCalcCount() );
        records.add(createRefMode() );
        records.add(createIteration() );
        records.add(createDelta() );
        records.add(createSaveRecalc() );
        gridset = createGridset();
        records.add( gridset );
        _gutsRecord = createGuts();
        records.add( _gutsRecord );
        records.add( createWSBool() );

        ColumnInfoRecordsAggregate columns = new ColumnInfoRecordsAggregate();
        records.add( columns );
        _columnInfos = columns;
        _dimensions = createDimensions();
        records.add(_dimensions);
        _rowsAggregate = new RowRecordsAggregate();
        records.add(_rowsAggregate);
        // 'Sheet View Settings'
        records.add(windowTwo = createWindowTwo());
        _selection = createSelection();
        records.add(_selection);

        records.add(_mergedCellsTable); // MCT comes after 'Sheet View Settings'
        records.add(EOFRecord.instance);

        _records = records;
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "Sheet createsheet from scratch exit");
    }

    public RowRecordsAggregate getRowsAggregate() {
        return _rowsAggregate;
    }

    /**
     * Adds a value record to the sheet's contained binary records
     * (i.e. LabelSSTRecord or NumberRecord).
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.
     *
     * @param row the row to add the cell value to
     * @param col the cell value record itself.
     */
    public void addValueRecord(int row, CellValueRecordInterface col) {

        if(log.check(POILogger.DEBUG)) {
          log.log(POILogger.DEBUG, "add value record  row" + row);
        }
        DimensionsRecord d = _dimensions;

        if (col.getColumn() > d.getLastCol()) {
            d.setLastCol(( short ) (col.getColumn() + 1));
        }
        if (col.getColumn() < d.getFirstCol()) {
            d.setFirstCol(col.getColumn());
        }
        _rowsAggregate.insertCell(col);
    }

    /**
     * replace a value record from the records array.
     *
     * This method is not loc sensitive, it resets loc to = dimsloc so no worries.
     *
     * @param newval - a record supporting the CellValueRecordInterface.  this will replace
     *                the cell value with the same row and column.  If there isn't one, one will
     *                be added.
     */

    public void replaceValueRecord(CellValueRecordInterface newval) {

        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "replaceValueRecord ");
        //The ValueRecordsAggregate use a tree map underneath.
        //The tree Map uses the CellValueRecordInterface as both the
        //key and the value, if we dont do a remove, then
        //the previous instance of the key is retained, effectively using
        //double the memory
        _rowsAggregate.removeCell(newval);
        _rowsAggregate.insertCell(newval);
    }

    /**
     * Adds a row record to the sheet
     *
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.
     *
     * @param row the row record to be added
     */

    public void addRow(RowRecord row) {
        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "addRow ");
        DimensionsRecord d = _dimensions;

        if (row.getRowNumber() >= d.getLastRow()) {
            d.setLastRow(row.getRowNumber() + 1);
        }
        if (row.getRowNumber() < d.getFirstRow()) {
            d.setFirstRow(row.getRowNumber());
        }

        //If the row exists remove it, so that any cells attached to the row are removed
        RowRecord existingRow = _rowsAggregate.getRow(row.getRowNumber());
        if (existingRow != null) {
            _rowsAggregate.removeRow(existingRow);
        }

        _rowsAggregate.insertRow(row);

        if (log.check( POILogger.DEBUG ))
            log.log(POILogger.DEBUG, "exit addRow");
    }


    /**
     * get the NEXT value record (from LOC).  The first record that is a value record
     * (starting at LOC) will be returned.
     *
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.  For this method, set loc to dimsloc to start with,
     * subsequent calls will return values in (physical) sequence or NULL when you get to the end.
     *
     * @return CellValueRecordInterface representing the next value record or NULL if there are no more
     */
    public CellValueRecordInterface[] getValueRecords() {
        return _rowsAggregate.getValueRecords();
    }

    /**
     * get the NEXT RowRecord (from LOC).  The first record that is a Row record
     * (starting at LOC) will be returned.
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.  For this method, set loc to dimsloc to start with.
     * subsequent calls will return rows in (physical) sequence or NULL when you get to the end.
     *
     * @return RowRecord representing the next row record or NULL if there are no more
     */
    public RowRecord getNextRow() {
        if (rowRecIterator == null)
        {
            rowRecIterator = _rowsAggregate.getIterator();
        }
        if (!rowRecIterator.hasNext())
        {
            return null;
        }
        return rowRecIterator.next();
    }


    /**
     * creates the BOF record
     */
    /* package */ static BOFRecord createBOF() {
        BOFRecord retval = new BOFRecord();

        retval.setVersion(( short ) 0x600);
        retval.setType(( short ) 0x010);

        retval.setBuild(( short ) 0x0dbb);
        retval.setBuildYear(( short ) 1996);
        retval.setHistoryBitMask(0xc1);
        retval.setRequiredVersion(0x6);
        return retval;
    }

    /**
     * creates the CalcMode record and sets it to 1 (automatic formula caculation)
     */
    private static CalcModeRecord createCalcMode() {
        CalcModeRecord retval = new CalcModeRecord();

        retval.setCalcMode(( short ) 1);
        return retval;
    }

    /**
     * creates the CalcCount record and sets it to 100 (default number of iterations)
     */
    private static CalcCountRecord createCalcCount() {
        CalcCountRecord retval = new CalcCountRecord();

        retval.setIterations(( short ) 100);   // default 100 iterations
        return retval;
    }

    /**
     * creates the RefMode record and sets it to A1 Mode (default reference mode)
     */
    private static RefModeRecord createRefMode() {
        RefModeRecord retval = new RefModeRecord();

        retval.setMode(RefModeRecord.USE_A1_MODE);
        return retval;
    }

    /**
     * creates the Iteration record and sets it to false (don't iteratively calculate formulas)
     */
    private static IterationRecord createIteration() {
        return new IterationRecord(false);
    }

    /**
     * creates the Delta record and sets it to 0.0010 (default accuracy)
     */
    private static DeltaRecord createDelta() {
        return new DeltaRecord(DeltaRecord.DEFAULT_VALUE);
    }

    /**
     * creates the SaveRecalc record and sets it to true (recalculate before saving)
     */
    private static SaveRecalcRecord createSaveRecalc() {
        SaveRecalcRecord retval = new SaveRecalcRecord();

        retval.setRecalc(true);
        return retval;
    }

    /**
     * creates the Gridset record and sets it to true (user has mucked with the gridlines)
     */
    private static GridsetRecord createGridset() {
        GridsetRecord retval = new GridsetRecord();

        retval.setGridset(true);
        return retval;
    }

    /**
     * creates the Guts record and sets leftrow/topcol guttter and rowlevelmax/collevelmax to 0
      */
    private static GutsRecord createGuts() {
        GutsRecord retval = new GutsRecord();

        retval.setLeftRowGutter(( short ) 0);
        retval.setTopColGutter(( short ) 0);
        retval.setRowLevelMax(( short ) 0);
        retval.setColLevelMax(( short ) 0);
        return retval;
    }

    /**
     * creates the WSBoolRecord and sets its values to defaults
     */
    private static WSBoolRecord createWSBool() {
        WSBoolRecord retval = new WSBoolRecord();

        retval.setWSBool1(( byte ) 0x4);
        retval.setWSBool2(( byte ) 0xffffffc1);
        return retval;
    }

    /**
     * get the index to the ExtendedFormatRecord "associated" with
     * the column at specified 0-based index. (In this case, an
     * ExtendedFormatRecord index is actually associated with a
     * ColumnInfoRecord which spans 1 or more columns)
     * <br/>
     * Returns the index to the default ExtendedFormatRecord (0xF)
     * if no ColumnInfoRecord exists that includes the column
     * index specified.
     * @param columnIndex
     * @return index of ExtendedFormatRecord associated with
     * ColumnInfoRecord that includes the column index or the
     * index of the default ExtendedFormatRecord (0xF)
     */
    public short getXFIndexForColAt(short columnIndex) {
        ColumnInfoRecord ci = _columnInfos.findColumnInfo(columnIndex);
        if (ci != null) {
            return (short)ci.getXFIndex();
        }
        return 0xF;
    }

    /**
     * creates the Dimensions Record and sets it to bogus values (you should set this yourself
     * or let the high level API do it for you)
     */
    private static DimensionsRecord createDimensions() {
        DimensionsRecord retval = new DimensionsRecord();

        retval.setFirstCol(( short ) 0);
        retval.setLastRow(1);             // one more than it is
        retval.setFirstRow(0);
        retval.setLastCol(( short ) 1);   // one more than it is
        return retval;
    }

    /**
     * creates the WindowTwo Record and sets it to:  <P>
     * options        = 0x6b6 <P>
     * toprow         = 0 <P>
     * leftcol        = 0 <P>
     * headercolor    = 0x40 <P>
     * pagebreakzoom  = 0x0 <P>
     * normalzoom     = 0x0 <p>
     */
    private static WindowTwoRecord createWindowTwo() {
        WindowTwoRecord retval = new WindowTwoRecord();

        retval.setOptions(( short ) 0x6b6);
        retval.setTopRow(( short ) 0);
        retval.setLeftCol(( short ) 0);
        retval.setHeaderColor(0x40);
        retval.setPageBreakZoom(( short ) 0);
        retval.setNormalZoom(( short ) 0);
        return retval;
    }

    /**
     * Creates the Selection record and sets it to nothing selected
    */
    private static SelectionRecord createSelection() {
        return new SelectionRecord(0, 0);
    }

    /**
     * Finds the first occurrence of a record matching a particular sid and
     * returns it's position.
     * @param sid   the sid to search for
     * @return  the record position of the matching record or -1 if no match
     *          is made.
     */
    public int findFirstRecordLocBySid( short sid ) { // TODO - remove this method
        int max = _records.size();
        for (int i=0; i< max; i++) {
            Object rb = _records.get(i);
            if (!(rb instanceof Record)) {
                continue;
            }
            Record record = (Record) rb;
            if (record.getSid() == sid) {
                return i;
            }
        }
        return -1;
    }
}
