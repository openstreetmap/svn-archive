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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDocument;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;


/**
 * High level representation of a workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 *
 * @see org.apache.poi.hssf.model.InternalWorkbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Shawn Laubach (slaubach at apache dot org)
 */
public final class HSSFWorkbook extends POIDocument implements org.apache.poi.ss.usermodel.Workbook {

    /**
     * used for compile-time performance/memory optimization.  This determines the
     * initial capacity for the sheet collection.  Its currently set to 3.
     * Changing it in this release will decrease performance
     * since you're never allowed to have more or less than three sheets!
     */

    public final static int INITIAL_CAPACITY = 3;

    /**
     * this is the reference to the low level Workbook object
     */

    private InternalWorkbook workbook;

    /**
     * this holds the HSSFSheet objects attached to this workbook
     */

    protected List<HSSFSheet> _sheets;

    /**
     * this holds the HSSFName objects attached to this workbook
     */

    private ArrayList<HSSFName> names;

    /**
     * The policy to apply in the event of missing or
     *  blank cells when fetching from a row.
     * See {@link MissingCellPolicy}
     */
    private MissingCellPolicy missingCellPolicy = HSSFRow.RETURN_NULL_AND_BLANK;

    private static POILogger log = POILogFactory.getLogger(HSSFWorkbook.class);

	private HSSFWorkbook(InternalWorkbook book) {
		super(null);
		workbook = book;
		_sheets = new ArrayList<HSSFSheet>(INITIAL_CAPACITY);
		names = new ArrayList<HSSFName>(INITIAL_CAPACITY);
	}

    public HSSFWorkbook(POIFSFileSystem fs) throws IOException {
        this(fs,true);
    }

    /**
     * given a POI POIFSFileSystem object, read in its Workbook and populate the high and
     * low level models.  If you're reading in a workbook...start here.
     *
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to. If set, will store all of the POIFSFileSystem
     *        in memory
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(POIFSFileSystem fs, boolean preserveNodes)
            throws IOException
    {
        this(fs.getRoot(), fs, preserveNodes);
    }

    /**
     * Normally, the Workbook will be in a POIFS Stream
     * called "Workbook". However, some weird XLS generators use "WORKBOOK"
     */
    private static final String[] WORKBOOK_DIR_ENTRY_NAMES = {
        "Workbook", // as per BIFF8 spec
        "WORKBOOK",
    };


    private static String getWorkbookDirEntryName(DirectoryNode directory) {

        String[] potentialNames = WORKBOOK_DIR_ENTRY_NAMES;
        for (int i = 0; i < potentialNames.length; i++) {
            String wbName = potentialNames[i];
            try {
                directory.getEntry(wbName);
                return wbName;
            } catch (FileNotFoundException e) {
                // continue - to try other options
            }
        }

        // check for previous version of file format
        try {
            directory.getEntry("Book");
            throw new OldExcelFormatException("The supplied spreadsheet seems to be Excel 5.0/7.0 (BIFF5) format. "
                    + "POI only supports BIFF8 format (from Excel versions 97/2000/XP/2003)");
        } catch (FileNotFoundException e) {
            // fall through
        }

        throw new IllegalArgumentException("The supplied POIFSFileSystem does not contain a BIFF8 'Workbook' entry. "
            + "Is it really an excel file?");
    }

    /**
     * given a POI POIFSFileSystem object, and a specific directory
     *  within it, read in its Workbook and populate the high and
     *  low level models.  If you're reading in a workbook...start here.
     *
     * @param directory the POI filesystem directory to process from
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to. If set, will store all of the POIFSFileSystem
     *        in memory
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(DirectoryNode directory, POIFSFileSystem fs, boolean preserveNodes)
            throws IOException
    {
        super(directory);
        String workbookName = getWorkbookDirEntryName(directory);

        // If we're not preserving nodes, don't track the
        //  POIFS any more
        if(! preserveNodes) {
           this.directory = null;
        }

        _sheets = new ArrayList<HSSFSheet>(INITIAL_CAPACITY);
        names  = new ArrayList<HSSFName>(INITIAL_CAPACITY);

        // Grab the data from the workbook stream, however
        //  it happens to be spelled.
        InputStream stream = directory.createDocumentInputStream(workbookName);

        List<Record> records = RecordFactory.createRecords(stream);

        workbook = InternalWorkbook.createWorkbook(records);
        setPropertiesFromWorkbook(workbook);
        int recOffset = workbook.getNumRecords();

        // convert all LabelRecord records to LabelSSTRecord
        convertLabelRecords(records, recOffset);
        RecordStream rs = new RecordStream(records, recOffset);
        while (rs.hasNext()) {
            InternalSheet sheet = InternalSheet.createSheet(rs);
            _sheets.add(new HSSFSheet(this, sheet));
        }

        for (int i = 0 ; i < workbook.getNumNames() ; ++i){
            NameRecord nameRecord = workbook.getNameRecord(i);
            HSSFName name = new HSSFName(this, nameRecord);
            names.add(name);
        }
    }


    /**
     * used internally to set the workbook properties.
     */

    private void setPropertiesFromWorkbook(InternalWorkbook book)
    {
        this.workbook = book;

        // none currently
    }

    /**
      * This is basically a kludge to deal with the now obsolete Label records.  If
      * you have to read in a sheet that contains Label records, be aware that the rest
      * of the API doesn't deal with them, the low level structure only provides read-only
      * semi-immutable structures (the sets are there for interface conformance with NO
      * impelmentation).  In short, you need to call this function passing it a reference
      * to the Workbook object.  All labels will be converted to LabelSST records and their
      * contained strings will be written to the Shared String tabel (SSTRecord) within
      * the Workbook.
      *
      * @param records a collection of sheet's records.
      * @param offset the offset to search at 
      * @see org.apache.poi.hssf.record.LabelRecord
      * @see org.apache.poi.hssf.record.LabelSSTRecord
      * @see org.apache.poi.hssf.record.SSTRecord
      */

     private void convertLabelRecords(List<Record> records, int offset)
     {
         if (log.check( POILogger.DEBUG ))
             log.log(POILogger.DEBUG, "convertLabelRecords called");
         for (int k = offset; k < records.size(); k++)
         {
             Record rec = records.get(k);

             if (rec.getSid() == LabelRecord.sid)
             {
                 LabelRecord oldrec = ( LabelRecord ) rec;

                 records.remove(k);
                 LabelSSTRecord newrec   = new LabelSSTRecord();
                 int            stringid =
                     workbook.addSSTString(new UnicodeString(oldrec.getValue()));

                 newrec.setRow(oldrec.getRow());
                 newrec.setColumn(oldrec.getColumn());
                 newrec.setXFIndex(oldrec.getXFIndex());
                 newrec.setSSTIndex(stringid);
                       records.add(k, newrec);
             }
         }
         if (log.check( POILogger.DEBUG ))
             log.log(POILogger.DEBUG, "convertLabelRecords exit");
     }

    /**
     * Retrieves the current policy on what to do when
     *  getting missing or blank cells from a row.
     * The default is to return blank and null cells.
     *  {@link MissingCellPolicy}
     */
    public MissingCellPolicy getMissingCellPolicy() {
        return missingCellPolicy;
    }

    private void validateSheetIndex(int index) {
        int lastSheetIx = _sheets.size() - 1;
        if (index < 0 || index > lastSheetIx) {
            throw new IllegalArgumentException("Sheet index ("
                    + index +") is out of range (0.." +    lastSheetIx + ")");
        }
    }



    /**
     * get the number of spreadsheets in the workbook (this will be three after serialization)
     * @return number of sheets
     */

    public int getNumberOfSheets() // NO_UCD
    {
        return _sheets.size();
    }

    /**
     * Get the HSSFSheet object at the given index.
     * @param index of the sheet number (0-based physical & logical)
     * @return HSSFSheet at the provided index
     */

    public HSSFSheet getSheetAt(int index)
    {
        validateSheetIndex(index);
        return _sheets.get(index);
    }

    /**
     * Get sheet with the given name (case insensitive match)
     * @param name of the sheet
     * @return HSSFSheet with the name provided or <code>null</code> if it does not exist
     */

    public HSSFSheet getSheet(String name) // NO_UCD
    {
        HSSFSheet retval = null;

        for (int k = 0; k < _sheets.size(); k++)
        {
            String sheetname = workbook.getSheetName(k);

            if (sheetname.equalsIgnoreCase(name))
            {
                retval = _sheets.get(k);
            }
        }
        return retval;
    }

    /**
     * get the number of styles the workbook contains
     * @return count of cell styles
     */


    InternalWorkbook getWorkbook() {
        return workbook;
    }
}
