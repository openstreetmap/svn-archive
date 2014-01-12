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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;

/**
 * High level representation of a cell in a row of a spreadsheet.
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot conatin numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells have the formula string, as well as
 * the formula result, which can be numeric or string.
 * <p>
 * Cells should have their number (0 based) before being added to a row.  Only
 * cells that have values should be added.
 * <p>
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Brian Sanders (kestrel at burdell dot org) Active Cell support
 * @author  Yegor Kozlov cell comments support
 */
public class HSSFCell implements Cell {

    private static final String FILE_FORMAT_NAME  = "BIFF8";
    /**
     * The maximum  number of columns in BIFF8
     */
    public static final int LAST_COLUMN_NUMBER  = SpreadsheetVersion.EXCEL97.getLastColumnIndex(); // 2^8 - 1
    private static final String LAST_COLUMN_NAME  = SpreadsheetVersion.EXCEL97.getLastColumnName();

    private final HSSFWorkbook       _book;
    private final HSSFSheet          _sheet;
    private int                      _cellType;
    private HSSFRichTextString       _stringValue;
    private CellValueRecordInterface _record;

    /**
     * Returns the HSSFSheet this cell belongs to
     *
     * @return the HSSFSheet that owns this cell
     */
    public HSSFSheet getSheet() {
        return _sheet;
    }

    /**
     * Returns the HSSFRow this cell belongs to
     *
     * @return the HSSFRow that owns this cell
     */
    public HSSFRow getRow() {
        int rowIndex = getRowIndex();
        return _sheet.getRow(rowIndex);
    }

    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param row   - the row of this cell
     * @param col   - the column for this cell
     * @param type  - CELL_TYPE_NUMERIC, CELL_TYPE_STRING, CELL_TYPE_FORMULA, CELL_TYPE_BLANK,
     *                CELL_TYPE_BOOLEAN, CELL_TYPE_ERROR
     *                Type of cell
     * @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(short,int)
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, int row, short col,
                       int type)
    {
        checkBounds(col);
        _cellType     = -1; // Force 'setCellType' to create a first Record
        _stringValue  = null;
        _book    = book;
        _sheet   = sheet;

        short xfindex = sheet.getSheet().getXFIndexForColAt(col);
        setCellType(type,false,row,col,xfindex);
    }

    /**
     * Creates an HSSFCell from a CellValueRecordInterface.  HSSFSheet uses this when
     * reading in cells from an existing sheet.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param cval - the Cell Value Record we wish to represent
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, CellValueRecordInterface cval) {
        _record      = cval;
        _cellType    = determineType(cval);
        _stringValue = null;
        _book   = book;
        _sheet  = sheet;
        switch (_cellType)
        {
            case CELL_TYPE_STRING :
                _stringValue = new HSSFRichTextString(book.getWorkbook(), (LabelSSTRecord ) cval);
                break;

            case CELL_TYPE_BLANK :
                break;

            case CELL_TYPE_FORMULA :
                _stringValue=new HSSFRichTextString(((FormulaRecordAggregate) cval).getStringValue());
                break;
        }
        ExtendedFormatRecord xf = book.getWorkbook().getExFormatAt(cval.getXFIndex());

        setCellStyle(new HSSFCellStyle(cval.getXFIndex(), xf, book));
    }


    /**
     * used internally -- given a cell value record, figure out its type
     */
    private static int determineType(CellValueRecordInterface cval) {
        if (cval instanceof FormulaRecordAggregate) {
            return HSSFCell.CELL_TYPE_FORMULA;
        }
        // all others are plain BIFF records
        Record record = ( Record ) cval;
        switch (record.getSid()) {

            case NumberRecord.sid :   return HSSFCell.CELL_TYPE_NUMERIC;
            case BlankRecord.sid :    return HSSFCell.CELL_TYPE_BLANK;
            case LabelSSTRecord.sid : return HSSFCell.CELL_TYPE_STRING;
            case BoolErrRecord.sid :
                BoolErrRecord boolErrRecord = ( BoolErrRecord ) record;

                return boolErrRecord.isBoolean()
                         ? HSSFCell.CELL_TYPE_BOOLEAN
                         : HSSFCell.CELL_TYPE_ERROR;
        }
        throw new RuntimeException("Bad cell value rec (" + cval.getClass().getName() + ")");
    }


    /**
     * @return the (zero based) index of the row containing this cell
     */
    public int getRowIndex() {
        return _record.getRow();
    }

    public int getColumnIndex() {
        return _record.getColumn() & 0xFFFF;
    }

    /**
     * set the cells type (numeric, formula or string)
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BLANK
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */
    public void setCellType(int cellType) {
        notifyFormulaChanging();
        if(isPartOfArrayFormulaGroup()){
            notifyArrayFormulaChanging();
        }
        int row=_record.getRow();
        short col=_record.getColumn();
        short styleIndex=_record.getXFIndex();
        setCellType(cellType, true, row, col, styleIndex);
    }

    /**
     * sets the cell type. The setValue flag indicates whether to bother about
     *  trying to preserve the current value in the new record if one is created.
     *  <p>
     *  The @see #setCellValue method will call this method with false in setValue
     *  since it will overwrite the cell value later
     *
     */

    private void setCellType(int cellType, boolean setValue, int row,short col, short styleIndex)
    {

        if (cellType > CELL_TYPE_ERROR)
        {
            throw new RuntimeException("I have no idea what type that is!");
        }
        switch (cellType)
        {

            case CELL_TYPE_FORMULA :
                FormulaRecordAggregate frec;

                if (cellType != _cellType) {
                    frec = _sheet.getSheet().getRowsAggregate().createFormula(row, col);
                } else {
                    frec = (FormulaRecordAggregate) _record;
                    frec.setRow(row);
                    frec.setColumn(col);
                }
                if (setValue)
                {
                    frec.getFormulaRecord().setValue(getNumericCellValue());
                }
                frec.setXFIndex(styleIndex);
                _record = frec;
                break;

            case CELL_TYPE_NUMERIC :
                NumberRecord nrec = null;

                if (cellType != _cellType)
                {
                    nrec = new NumberRecord();
                }
                else
                {
                    nrec = ( NumberRecord ) _record;
                }
                nrec.setColumn(col);
                if (setValue)
                {
                    nrec.setValue(getNumericCellValue());
                }
                nrec.setXFIndex(styleIndex);
                nrec.setRow(row);
                _record = nrec;
                break;

            case CELL_TYPE_STRING :
                LabelSSTRecord lrec;

                if (cellType == _cellType) {
                    lrec = (LabelSSTRecord) _record;
                } else {
                    lrec = new LabelSSTRecord();
                    lrec.setColumn(col);
                    lrec.setRow(row);
                    lrec.setXFIndex(styleIndex);
                }
                if (setValue) {
                    String str = convertCellValueToString();
                    int sstIndex = _book.getWorkbook().addSSTString(new UnicodeString(str));
                    lrec.setSSTIndex(sstIndex);
                    UnicodeString us = _book.getWorkbook().getSSTString(sstIndex);
                    _stringValue = new HSSFRichTextString();
                    _stringValue.setUnicodeString(us);
                }
                _record = lrec;
                break;

            case CELL_TYPE_BLANK :
                BlankRecord brec = null;

                if (cellType != _cellType)
                {
                    brec = new BlankRecord();
                }
                else
                {
                    brec = ( BlankRecord ) _record;
                }
                brec.setColumn(col);

                // During construction the cellStyle may be null for a Blank cell.
                brec.setXFIndex(styleIndex);
                brec.setRow(row);
                _record = brec;
                break;

            case CELL_TYPE_BOOLEAN :
                BoolErrRecord boolRec = null;

                if (cellType != _cellType)
                {
                    boolRec = new BoolErrRecord();
                }
                else
                {
                    boolRec = ( BoolErrRecord ) _record;
                }
                boolRec.setColumn(col);
                if (setValue)
                {
                    boolRec.setValue(convertCellValueToBoolean());
                }
                boolRec.setXFIndex(styleIndex);
                boolRec.setRow(row);
                _record = boolRec;
                break;

            case CELL_TYPE_ERROR :
                BoolErrRecord errRec = null;

                if (cellType != _cellType)
                {
                    errRec = new BoolErrRecord();
                }
                else
                {
                    errRec = ( BoolErrRecord ) _record;
                }
                errRec.setColumn(col);
                if (setValue)
                {
                    errRec.setValue((byte)HSSFErrorConstants.ERROR_VALUE);
                }
                errRec.setXFIndex(styleIndex);
                errRec.setRow(row);
                _record = errRec;
                break;
        }
        if (cellType != _cellType &&
            _cellType!=-1 )  // Special Value to indicate an uninitialized Cell
        {
            _sheet.getSheet().replaceValueRecord(_record);
        }
        _cellType = cellType;
    }

    /**
     * get the cells type (numeric, formula or string)
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */

    public int getCellType()
    {
        return _cellType;
    }


    /**
     * Should be called any time that a formula could potentially be deleted.
     * Does nothing if this cell currently does not hold a formula
     */
    private void notifyFormulaChanging() {
        if (_record instanceof FormulaRecordAggregate) {
            ((FormulaRecordAggregate)_record).notifyFormulaChanging();
        }
    }

    public String getCellFormula() {
        if (!(_record instanceof FormulaRecordAggregate)) {
            throw typeMismatch(CELL_TYPE_FORMULA, _cellType, true);
        }
        return HSSFFormulaParser.toFormulaString(_book, ((FormulaRecordAggregate)_record).getFormulaTokens());
    }

    /**
     * Used to help format error messages
     */
    private static String getCellTypeName(int cellTypeCode) {
        switch (cellTypeCode) {
            case CELL_TYPE_BLANK:   return "blank";
            case CELL_TYPE_STRING:  return "text";
            case CELL_TYPE_BOOLEAN: return "boolean";
            case CELL_TYPE_ERROR:   return "error";
            case CELL_TYPE_NUMERIC: return "numeric";
            case CELL_TYPE_FORMULA: return "formula";
        }
        return "#unknown cell type (" + cellTypeCode + ")#";
    }

    private static RuntimeException typeMismatch(int expectedTypeCode, int actualTypeCode, boolean isFormulaCell) {
        String msg = "Cannot get a "
            + getCellTypeName(expectedTypeCode) + " value from a "
            + getCellTypeName(actualTypeCode) + " " + (isFormulaCell ? "formula " : "") + "cell";
        return new IllegalStateException(msg);
    }
    private static void checkFormulaCachedValueType(int expectedTypeCode, FormulaRecord fr) {
        int cachedValueType = fr.getCachedResultType();
        if (cachedValueType != expectedTypeCode) {
            throw typeMismatch(expectedTypeCode, cachedValueType, true);
        }
    }

    /**
     * Get the value of the cell as a number.
     * For strings we throw an exception.
     * For blank cells we return a 0.
     * See {@link HSSFDataFormatter} for turning this
     *  number into a string similar to that which
     *  Excel would render this number as.
     */
    public double getNumericCellValue() {

        switch(_cellType) {
            case CELL_TYPE_BLANK:
                return 0.0;
            case CELL_TYPE_NUMERIC:
                return ((NumberRecord)_record).getValue();
            default:
                throw typeMismatch(CELL_TYPE_NUMERIC, _cellType, false);
            case CELL_TYPE_FORMULA:
                break;
        }
        FormulaRecord fr = ((FormulaRecordAggregate)_record).getFormulaRecord();
        checkFormulaCachedValueType(CELL_TYPE_NUMERIC, fr);
        return fr.getValue();
    }

    /**
     * Get the value of the cell as a date.
     * For strings we throw an exception.
     * For blank cells we return a null.
     * See {@link HSSFDataFormatter} for formatting
     *  this date into a string similar to how excel does.
     */
    public Date getDateCellValue() {

        if (_cellType == CELL_TYPE_BLANK) {
            return null;
        }
        double value = getNumericCellValue();
        if (_book.getWorkbook().isUsing1904DateWindowing()) {
            return DateUtil.getJavaDate(value, true);
        }
        return DateUtil.getJavaDate(value, false);
    }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     */
    public String getStringCellValue()
    {
      HSSFRichTextString str = getRichStringCellValue();
      return str.getString();
    }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     */
    public HSSFRichTextString getRichStringCellValue() {

        switch(_cellType) {
            case CELL_TYPE_BLANK:
                return new HSSFRichTextString("");
            case CELL_TYPE_STRING:
                return _stringValue;
            default:
                throw typeMismatch(CELL_TYPE_STRING, _cellType, false);
            case CELL_TYPE_FORMULA:
                break;
        }
        FormulaRecordAggregate fra = ((FormulaRecordAggregate)_record);
        checkFormulaCachedValueType(CELL_TYPE_STRING, fra.getFormulaRecord());
        String strVal = fra.getStringValue();
        return new HSSFRichTextString(strVal == null ? "" : strVal);
    }



    /**
     * Chooses a new boolean value for the cell when its type is changing.<p/>
     *
     * Usually the caller is calling setCellType() with the intention of calling
     * setCellValue(boolean) straight afterwards.  This method only exists to give
     * the cell a somewhat reasonable value until the setCellValue() call (if at all).
     * TODO - perhaps a method like setCellTypeAndValue(int, Object) should be introduced to avoid this
     */
    private boolean convertCellValueToBoolean() {

        switch (_cellType) {
            case CELL_TYPE_BOOLEAN:
                return (( BoolErrRecord ) _record).getBooleanValue();
            case CELL_TYPE_STRING:
                int sstIndex = ((LabelSSTRecord)_record).getSSTIndex();
                String text = _book.getWorkbook().getSSTString(sstIndex).getString();
                return Boolean.valueOf(text).booleanValue();
            case CELL_TYPE_NUMERIC:
                return ((NumberRecord)_record).getValue() != 0;

            case CELL_TYPE_FORMULA:
                // use cached formula result if it's the right type:
                FormulaRecord fr = ((FormulaRecordAggregate)_record).getFormulaRecord();
                checkFormulaCachedValueType(CELL_TYPE_BOOLEAN, fr);
                return fr.getCachedBooleanValue();
            // Other cases convert to false
            // These choices are not well justified.
            case CELL_TYPE_ERROR:
            case CELL_TYPE_BLANK:
                return false;
        }
        throw new RuntimeException("Unexpected cell type (" + _cellType + ")");
    }
    private String convertCellValueToString() {

        switch (_cellType) {
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return ((BoolErrRecord) _record).getBooleanValue() ? "TRUE" : "FALSE";
            case CELL_TYPE_STRING:
                int sstIndex = ((LabelSSTRecord)_record).getSSTIndex();
                return _book.getWorkbook().getSSTString(sstIndex).getString();
            case CELL_TYPE_NUMERIC:
                return NumberToTextConverter.toText(((NumberRecord)_record).getValue());
            case CELL_TYPE_ERROR:
                   return HSSFErrorConstants.getText(((BoolErrRecord) _record).getErrorValue());
            case CELL_TYPE_FORMULA:
                // should really evaluate, but HSSFCell can't call HSSFFormulaEvaluator
                // just use cached formula result instead
                break;
            default:
                throw new IllegalStateException("Unexpected cell type (" + _cellType + ")");
        }
        FormulaRecordAggregate fra = ((FormulaRecordAggregate)_record);
        FormulaRecord fr = fra.getFormulaRecord();
        switch (fr.getCachedResultType()) {
            case CELL_TYPE_BOOLEAN:
                return fr.getCachedBooleanValue() ? "TRUE" : "FALSE";
            case CELL_TYPE_STRING:
                return fra.getStringValue();
            case CELL_TYPE_NUMERIC:
                return NumberToTextConverter.toText(fr.getValue());
            case CELL_TYPE_ERROR:
                   return HSSFErrorConstants.getText(fr.getCachedErrorValue());
        }
        throw new IllegalStateException("Unexpected formula result type (" + _cellType + ")");
    }

    /**
     * get the value of the cell as a boolean.  For strings, numbers, and errors, we throw an exception.
     * For blank cells we return a false.
     */
    public boolean getBooleanCellValue() {

        switch(_cellType) {
            case CELL_TYPE_BLANK:
                return false;
            case CELL_TYPE_BOOLEAN:
                return (( BoolErrRecord ) _record).getBooleanValue();
            default:
                throw typeMismatch(CELL_TYPE_BOOLEAN, _cellType, false);
            case CELL_TYPE_FORMULA:
                break;
        }
        FormulaRecord fr = ((FormulaRecordAggregate)_record).getFormulaRecord();
        checkFormulaCachedValueType(CELL_TYPE_BOOLEAN, fr);
        return fr.getCachedBooleanValue();
    }

    public void setCellStyle(HSSFCellStyle style) {
        // Verify it really does belong to our workbook
        style.verifyBelongsToWorkbook(_book);

        // Change our cell record to use this style
        _record.setXFIndex(style.getIndex());
    }

    /**
     * get the style for the cell.  This is a reference to a cell style contained in the workbook
     * object.
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
     */
    public HSSFCellStyle getCellStyle()
    {
      short styleIndex=_record.getXFIndex();
      ExtendedFormatRecord xf = _book.getWorkbook().getExFormatAt(styleIndex);
      return new HSSFCellStyle(styleIndex, xf, _book);
    }

    /**
     * Should only be used by HSSFSheet and friends.  Returns the low level CellValueRecordInterface record
     *
     * @return CellValueRecordInterface representing the cell via the low level api.
     */

    protected CellValueRecordInterface getCellValueRecord()
    {
        return _record;
    }

    /**
     * @throws RuntimeException if the bounds are exceeded.
     */
    private static void checkBounds(int cellIndex) {
        if (cellIndex < 0 || cellIndex > LAST_COLUMN_NUMBER) {
            throw new IllegalArgumentException("Invalid column index (" + cellIndex
                    + ").  Allowable column range for " + FILE_FORMAT_NAME + " is (0.."
                    + LAST_COLUMN_NUMBER + ") or ('A'..'" + LAST_COLUMN_NAME + "')");
        }
    }


    /**
     * Returns a string representation of the cell
     *
     * This method returns a simple representation,
     * anthing more complex should be in user code, with
     * knowledge of the semantics of the sheet being processed.
     *
     * Formula cells return the formula string,
     * rather than the formula result.
     * Dates are displayed in dd-MMM-yyyy format
     * Errors are displayed as #ERR&lt;errIdx&gt;
     */
    public String toString() {
        switch (getCellType()) {
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return getBooleanCellValue()?"TRUE":"FALSE";
            case CELL_TYPE_ERROR:
                return ErrorEval.getText((( BoolErrRecord ) _record).getErrorValue());
            case CELL_TYPE_FORMULA:
                return getCellFormula();
            case CELL_TYPE_NUMERIC:
                //TODO apply the dataformat for this cell
                if (DateUtil.isCellDateFormatted(this)) {
                    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                    return sdf.format(getDateCellValue());
                }
				return  String.valueOf(getNumericCellValue());
            case CELL_TYPE_STRING:
                return getStringCellValue();
            default:
                return "Unknown Cell Type: " + getCellType();
        }
    }

    public CellRangeAddress getArrayFormulaRange() {
        if (_cellType != CELL_TYPE_FORMULA) {
            String ref = new CellReference(this).formatAsString();
            throw new IllegalStateException("Cell " + ref
                    + " is not part of an array formula.");
        }
        return ((FormulaRecordAggregate)_record).getArrayFormulaRange();
    }

    public boolean isPartOfArrayFormulaGroup() {
        if (_cellType != CELL_TYPE_FORMULA) {
            return false;
        }
        return ((FormulaRecordAggregate)_record).isPartOfArrayFormula();
    }

    /**
     * The purpose of this method is to validate the cell state prior to modification
     *
     * @see #notifyArrayFormulaChanging()
     */
    void notifyArrayFormulaChanging(String msg){
        CellRangeAddress cra = getArrayFormulaRange();
        if(cra.getNumberOfCells() > 1) {
            throw new IllegalStateException(msg);
        }
        //un-register the single-cell array formula from the parent XSSFSheet
        getRow().getSheet().removeArrayFormula(this);
    }

    /**
     * Called when this cell is modified.
     * <p>
     * The purpose of this method is to validate the cell state prior to modification.
     * </p>
     *
     * @see #setCellType(int)
     * @see #setCellFormula(String)
     * @see HSSFRow#removeCell(org.apache.poi.ss.usermodel.Cell)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#removeRow(org.apache.poi.ss.usermodel.Row)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#shiftRows(int, int, int)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#addMergedRegion(org.apache.poi.ss.util.CellRangeAddress)
     * @throws IllegalStateException if modification is not allowed
     */
    void notifyArrayFormulaChanging(){
        CellReference ref = new CellReference(this);
        String msg = "Cell "+ref.formatAsString()+" is part of a multi-cell array formula. " +
                "You cannot change part of an array.";
        notifyArrayFormulaChanging(msg);
    }

}
