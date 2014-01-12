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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.DateWindow1904Record;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.ExtSSTRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.hssf.record.NameCommentRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.record.TabIdRecord;
import org.apache.poi.hssf.record.WindowOneRecord;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Low level model implementation of a Workbook.  Provides creational methods
 * for settings and objects contained in the workbook object.
 * <P>
 * This file contains the low level binary records starting at the workbook's BOF and
 * ending with the workbook's EOF.  Use HSSFWorkbook for a high level representation.
 * <P>
 * The structures of the highlevel API use references to this to perform most of their
 * operations.  Its probably unwise to use these low level structures directly unless you
 * really know what you're doing.  I recommend you read the Microsoft Excel 97 Developer's
 * Kit (Microsoft Press) and the documentation at http://sc.openoffice.org/excelfileformat.pdf
 * before even attempting to use this.
 *
 *
 * @author  Luc Girardin (luc dot girardin at macrofocus dot com)
 * @author  Sergei Kozello (sergeikozello at mail.ru)
 * @author  Shawn Laubach (slaubach at apache dot org) (Data Formats)
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Brian Sanders (bsanders at risklabs dot com) - custom palette
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
 */
@Internal
public final class InternalWorkbook {


    private static final POILogger log = POILogFactory.getLogger(InternalWorkbook.class);
    private static final int DEBUG = POILogger.DEBUG;

    /**
     * this contains the Worksheet record objects
     */
    private final WorkbookRecordList records;

    /**
     * this contains a reference to the SSTRecord so that new stings can be added
     * to it.
     */
    protected SSTRecord sst;


    private LinkTable linkTable; // optionally occurs if there are  references in the document. (4.10.3)

    /**
     * holds the "boundsheet" records (aka bundlesheet) so that they can have their
     * reference to their "BOF" marker
     */
    private final List<BoundSheetRecord> boundsheets;
    private final List<FormatRecord> formats;
    private final List<HyperlinkRecord> hyperlinks;

    /** the number of extended format records */
	private int numxfs;
    /** holds the max format id */
	private int maxformatid;
    /** whether 1904 date windowing is being used */
    private boolean uses1904datewindowing;
    private WindowOneRecord windowOne;

    /**
     * Hold the {@link NameCommentRecord}s indexed by the name of the {@link NameRecord} to which they apply.
     */
    private final Map<String, NameCommentRecord> commentRecords;

    private InternalWorkbook() {
    	records     = new WorkbookRecordList();

		boundsheets = new ArrayList<BoundSheetRecord>();
		formats = new ArrayList<FormatRecord>();
		hyperlinks = new ArrayList<HyperlinkRecord>();
		numxfs = 0;
		maxformatid = -1;
		uses1904datewindowing = false;
		commentRecords = new LinkedHashMap<String, NameCommentRecord>();
    }

    /**
     * read support  for low level
     * API.  Pass in an array of Record objects, A Workbook
     * object is constructed and passed back with all of its initialization set
     * to the passed in records and references to those records held. Unlike Sheet
     * workbook does not use an offset (its assumed to be 0) since its first in a file.
     * If you need an offset then construct a new array with a 0 offset or write your
     * own ;-p.
     *
     * @param recs an array of Record objects
     * @return Workbook object
     */
    public static InternalWorkbook createWorkbook(List<Record> recs) {
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "Workbook (readfile) created with reclen=",
                    Integer.valueOf(recs.size()));
        InternalWorkbook retval = new InternalWorkbook();
        List<Record> records = new ArrayList<Record>(recs.size() / 3);
        retval.records.setRecords(records);

        int k;
        for (k = 0; k < recs.size(); k++) {
            Record rec = recs.get(k);

            if (rec.getSid() == EOFRecord.sid) {
                records.add(rec);
                if (log.check( POILogger.DEBUG ))
                    log.log(DEBUG, "found workbook eof record at " + k);
                break;
            }
            switch (rec.getSid()) {

                case BoundSheetRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found boundsheet record at " + k);
                    retval.boundsheets.add((BoundSheetRecord) rec);
                    retval.records.setBspos( k );
                    break;

                case SSTRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found sst record at " + k);
                    retval.sst = ( SSTRecord ) rec;
                    break;

                case ExtendedFormatRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found XF record at " + k);
                    retval.records.setXfpos( k );
                    retval.numxfs++;
                    break;

                case TabIdRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found tabid record at " + k);
                    retval.records.setTabpos( k );
                    break;

                case ExternSheetRecord.sid :
                    throw new RuntimeException("Extern sheet is part of LinkTable");
                case NameRecord.sid :
                case SupBookRecord.sid :
                    // LinkTable can start with either of these
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found SupBook record at " + k);
                    retval.linkTable = new LinkTable(recs, k, retval.records, retval.commentRecords);
                    k+=retval.linkTable.getRecordCount() - 1;
                    continue;
                case FormatRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found format record at " + k);
                    retval.formats.add((FormatRecord) rec);
                    retval.maxformatid = retval.maxformatid >= ((FormatRecord)rec).getIndexCode() ? retval.maxformatid : ((FormatRecord)rec).getIndexCode();
                    break;
                case DateWindow1904Record.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found datewindow1904 record at " + k);
                    retval.uses1904datewindowing = ((DateWindow1904Record)rec).getWindowing() == 1;
                    break;
                case WindowOneRecord.sid:
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found WindowOneRecord at " + k);
                    retval.windowOne = (WindowOneRecord) rec;
                    break;
                case NameCommentRecord.sid:
                    final NameCommentRecord ncr = (NameCommentRecord) rec;
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found NameComment at " + k);
                    retval.commentRecords.put(ncr.getNameText(), ncr);
                default :
            }
            records.add(rec);
        }
        //What if we dont have any ranges and supbooks
        //        if (retval.records.supbookpos == 0) {
        //            retval.records.supbookpos = retval.records.bspos + 1;
        //            retval.records.namepos    = retval.records.supbookpos + 1;
        //        }

        // Look for other interesting values that
        //  follow the EOFRecord
        for ( ; k < recs.size(); k++) {
            Record rec = recs.get(k);
            switch (rec.getSid()) {
                case HyperlinkRecord.sid:
                    retval.hyperlinks.add((HyperlinkRecord)rec);
                    break;
            }
        }

        if (retval.windowOne == null) {
            retval.windowOne = createWindowOne();
        }
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "exit create workbook from existing file function");
        return retval;
    }

    public int getNumRecords() {
        return records.size();
    }


    private BoundSheetRecord getBoundSheetRec(int sheetIndex) {
        return boundsheets.get(sheetIndex);
    }


    /**
     * gets the name for a given sheet.
     *
     * @param sheetIndex the sheet number (0 based)
     * @return sheetname the name for the sheet
     */
    public String getSheetName(int sheetIndex) {
        return getBoundSheetRec(sheetIndex).getSheetname();
    }

    /**
     * gets the ExtendedFormatRecord at the given 0-based index
     *
     * @param index of the Extended format record (0-based)
     * @return ExtendedFormatRecord at the given index
     */

    public ExtendedFormatRecord getExFormatAt(int index) {
        int xfptr = records.getXfpos() - (numxfs - 1);

        xfptr += index;
        ExtendedFormatRecord retval =
        ( ExtendedFormatRecord ) records.get(xfptr);

        return retval;
    }

    /**
     * Adds a string to the SST table and returns its index (if its a duplicate
     * just returns its index and update the counts) ASSUMES compressed unicode
     * (meaning 8bit)
     *
     * @param string the string to be added to the SSTRecord
     *
     * @return index of the string within the SSTRecord
     */

    public int addSSTString(UnicodeString string) {
        if (log.check( POILogger.DEBUG ))
          log.log(DEBUG, "insert to sst string='", string);
        if (sst == null) {
            insertSST();
        }
      return sst.addString(string);
    }

    /**
     * given an index into the SST table, this function returns the corresponding String value
     * @return String containing the SST String
     */

    public UnicodeString getSSTString(int str) {
        if (sst == null) {
            insertSST();
        }
        UnicodeString retval = sst.getString(str);

        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "Returning SST for index=", Integer.valueOf(str),
                " String= ", retval);
        return retval;
    }

    /**
     * use this function to add a Shared String Table to an existing sheet (say
     * generated by a different java api) without an sst....
     * @see #createExtendedSST()
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    public void insertSST() {
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "creating new SST via insertSST!");
        sst = new SSTRecord();
        records.add(records.size() - 1, createExtendedSST());
        records.add(records.size() - 2, sst);
    }

    /**
     * Serializes all records int the worksheet section into a big byte array. Use
     * this to write the Workbook out.
     *
     * @return byte array containing the HSSF-only portions of the POIFS file.
     */
     // GJS: Not used so why keep it.
//    public byte [] serialize() {
//        log.log(DEBUG, "Serializing Workbook!");
//        byte[] retval    = null;
//
////         ArrayList bytes     = new ArrayList(records.size());
//        int    arraysize = getSize();
//        int    pos       = 0;
//
//        retval = new byte[ arraysize ];
//        for (int k = 0; k < records.size(); k++) {
//
//            Record record = records.get(k);
////             Let's skip RECALCID records, as they are only use for optimization
//        if(record.getSid() != RecalcIdRecord.sid || ((RecalcIdRecord)record).isNeeded()) {
//                pos += record.serialize(pos, retval);   // rec.length;
//        }
//        }
//        log.log(DEBUG, "Exiting serialize workbook");
//        return retval;
//    }

    /**
     * creates the WindowOne record with the following magic values: <P>
     * horizontal hold - 0x168 <P>
     * vertical hold   - 0x10e <P>
     * width           - 0x3a5c <P>
     * height          - 0x23be <P>
     * options         - 0x38 <P>
     * selected tab    - 0 <P>
     * displayed tab   - 0 <P>
     * num selected tab- 0 <P>
     * tab width ratio - 0x258 <P>
     */
    private static WindowOneRecord createWindowOne() {
        WindowOneRecord retval = new WindowOneRecord();

        retval.setHorizontalHold(( short ) 0x168);
        retval.setVerticalHold(( short ) 0x10e);
        retval.setWidth(( short ) 0x3a5c);
        retval.setHeight(( short ) 0x23be);
        retval.setOptions(( short ) 0x38);
        retval.setActiveSheetIndex( 0x0);
        retval.setFirstVisibleTab(0x0);
        retval.setNumSelectedTabs(( short ) 1);
        retval.setTabWidthRatio(( short ) 0x258);
        return retval;
    }

    /**
     * Creates the ExtendedSST record with numstrings per bucket set to 0x8.  HSSF
     * doesn't yet know what to do with this thing, but we create it with nothing in
     * it hardly just to make Excel happy and our sheets look like Excel's
     */
    private static ExtSSTRecord createExtendedSST() {
        ExtSSTRecord retval = new ExtSSTRecord();
        retval.setNumStringsPerBucket(( short ) 0x8);
        return retval;
    }

    /** finds the sheet name by his extern sheet index
     * @param externSheetIndex extern sheet index
     * @return sheet name.
     */
    public String findSheetNameFromExternSheet(int externSheetIndex){

        int indexToSheet = linkTable.getIndexToInternalSheet(externSheetIndex);
        if (indexToSheet < 0) {
            // TODO - what does '-1' mean here?
            //error check, bail out gracefully!
            return "";
        }
        if (indexToSheet >= boundsheets.size()) {
            // Not sure if this can ever happen (See bug 45798)
            return ""; // Seems to be what excel would do in this case
        }
        return getSheetName(indexToSheet);
    }
    public ExternalSheet getExternalSheet(int externSheetIndex) {
        String[] extNames = linkTable.getExternalBookAndSheetName(externSheetIndex);
        if (extNames == null) {
            return null;
        }
        return new ExternalSheet(extNames[0], extNames[1]);
    }

    /** gets the total number of names
     * @return number of names
     */
    public int getNumNames(){
        if(linkTable == null) {
            return 0;
        }
        return linkTable.getNumNames();
    }

    /** gets the name record
     * @param index name index
     * @return name record
     */
    public NameRecord getNameRecord(int index){
        return linkTable.getNameRecord(index);
    }



    /**
     * Returns the list of FormatRecords in the workbook.
     * @return ArrayList of FormatRecords in the notebook
     */
    public List<FormatRecord> getFormats() {
      return formats;
    }

    /**
    * Whether date windowing is based on 1/2/1904 or 1/1/1900.
    * Some versions of Excel (Mac) can save workbooks using 1904 date windowing.
    *
    * @return true if using 1904 date windowing
    */
    public boolean isUsing1904DateWindowing() {
        return uses1904datewindowing;
    }

    /**
     * @param refIndex Index to REF entry in EXTERNSHEET record in the Link Table
     * @param definedNameIndex zero-based to DEFINEDNAME or EXTERNALNAME record
     * @return the string representation of the defined or external name
     */
    public String resolveNameXText(int refIndex, int definedNameIndex) {
        return linkTable.resolveNameXText(refIndex, definedNameIndex);
    }
}
