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
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.CRNCountRecord;
import org.apache.poi.hssf.record.CRNRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.ExternalNameRecord;
import org.apache.poi.hssf.record.NameCommentRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;

/**
 * Link Table (OOO pdf reference: 4.10.3 ) <p/>
 *
 * The main data of all types of references is stored in the Link Table inside the Workbook Globals
 * Substream (4.2.5). The Link Table itself is optional and occurs only, if  there are any
 * references in the document.
 *  <p/>
 *
 *  In BIFF8 the Link Table consists of
 *  <ul>
 *  <li>zero or more EXTERNALBOOK Blocks<p/>
 *  	each consisting of
 *  	<ul>
 *  	<li>exactly one EXTERNALBOOK (0x01AE) record</li>
 *  	<li>zero or more EXTERNALNAME (0x0023) records</li>
 *  	<li>zero or more CRN Blocks<p/>
 *			each consisting of
 *  		<ul>
 *  		<li>exactly one XCT (0x0059)record</li>
 *  		<li>zero or more CRN (0x005A) records (documentation says one or more)</li>
 *  		</ul>
 *  	</li>
 *  	</ul>
 *  </li>
 *  <li>zero or one EXTERNSHEET (0x0017) record</li>
 *  <li>zero or more DEFINEDNAME (0x0018) records</li>
 *  </ul>
 *
 *
 * @author Josh Micich
 */
final class LinkTable {


	// TODO make this class into a record aggregate

	private static final class CRNBlock {

		private final CRNCountRecord _countRecord;

		public CRNBlock(RecordStream rs) {
			_countRecord = (CRNCountRecord) rs.getNext();
			int nCRNs = _countRecord.getNumberOfCRNs();
			CRNRecord[] crns = new CRNRecord[nCRNs];
			for (int i = 0; i < crns.length; i++) {
				crns[i] = (CRNRecord) rs.getNext();
			}
		}
	}

	private static final class ExternalBookBlock {
		private final SupBookRecord _externalBookRecord;
		private final ExternalNameRecord[] _externalNameRecords;
		private final CRNBlock[] _crnBlocks;

		public ExternalBookBlock(RecordStream rs) {
			_externalBookRecord = (SupBookRecord) rs.getNext();
			List<Object> temp = new ArrayList<Object>();
			while(rs.peekNextClass() == ExternalNameRecord.class) {
			   temp.add(rs.getNext());
			}
			_externalNameRecords = new ExternalNameRecord[temp.size()];
			temp.toArray(_externalNameRecords);

			temp.clear();

			while(rs.peekNextClass() == CRNCountRecord.class) {
				temp.add(new CRNBlock(rs));
			}
			_crnBlocks = new CRNBlock[temp.size()];
			temp.toArray(_crnBlocks);
		}

		public SupBookRecord getExternalBookRecord() {
			return _externalBookRecord;
		}

		public String getNameText(int definedNameIndex) {
			return _externalNameRecords[definedNameIndex].getText();
		}

	}

	private final ExternalBookBlock[] _externalBookBlocks;
	private final ExternSheetRecord _externSheetRecord;
	private final List<NameRecord> _definedNames;
	private final int _recordCount;
	private final WorkbookRecordList _workbookRecordList; // TODO - would be nice to remove this

	public LinkTable(List<Record> inputList, int startIndex, WorkbookRecordList workbookRecordList, Map<String, NameCommentRecord> commentRecords) {

		_workbookRecordList = workbookRecordList;
		RecordStream rs = new RecordStream(inputList, startIndex);

		List<ExternalBookBlock> temp = new ArrayList<ExternalBookBlock>();
		while(rs.peekNextClass() == SupBookRecord.class) {
		   temp.add(new ExternalBookBlock(rs));
		}

		_externalBookBlocks = new ExternalBookBlock[temp.size()];
		temp.toArray(_externalBookBlocks);
		temp.clear();

		if (_externalBookBlocks.length > 0) {
			// If any ExternalBookBlock present, there is always 1 of ExternSheetRecord
			if (rs.peekNextClass() != ExternSheetRecord.class) {
				// not quite - if written by google docs
				_externSheetRecord = null;
			} else {
				_externSheetRecord = readExtSheetRecord(rs);
			}
		} else {
			_externSheetRecord = null;
		}

		_definedNames = new ArrayList<NameRecord>();
		// collect zero or more DEFINEDNAMEs id=0x18,
		//  with their comments if present
		while(true) {
		  Class<? extends Record> nextClass = rs.peekNextClass();
		  if (nextClass == NameRecord.class) {
		    NameRecord nr = (NameRecord)rs.getNext();
		    _definedNames.add(nr);
		  }
		  else if (nextClass == NameCommentRecord.class) {
		    NameCommentRecord ncr = (NameCommentRecord)rs.getNext();
		    commentRecords.put(ncr.getNameText(), ncr);
		  }
		  else {
		    break;
		  }
		}

		_recordCount = rs.getCountRead();
		_workbookRecordList.getRecords().addAll(inputList.subList(startIndex, startIndex + _recordCount));
	}

	private static ExternSheetRecord readExtSheetRecord(RecordStream rs) {
		List<ExternSheetRecord> temp = new ArrayList<ExternSheetRecord>(2);
		while(rs.peekNextClass() == ExternSheetRecord.class) {
			temp.add((ExternSheetRecord) rs.getNext());
		}

		int nItems = temp.size();
		if (nItems < 1) {
			throw new RuntimeException("Expected an EXTERNSHEET record but got ("
					+ rs.peekNextClass().getName() + ")");
		}
		if (nItems == 1) {
			// this is the normal case. There should be just one ExternSheetRecord
			return temp.get(0);
		}
		// Some apps generate multiple ExternSheetRecords (see bug 45698).
		// It seems like the best thing to do might be to combine these into one
		ExternSheetRecord[] esrs = new ExternSheetRecord[nItems];
		temp.toArray(esrs);
		return ExternSheetRecord.combine(esrs);
	}

	/**
	 * TODO - would not be required if calling code used RecordStream or similar
	 */
	public int getRecordCount() {
		return _recordCount;
	}

	public int getNumNames() {
		return _definedNames.size();
	}

	public NameRecord getNameRecord(int index) {
		return _definedNames.get(index);
	}

	public String[] getExternalBookAndSheetName(int extRefIndex) {
		int ebIx = _externSheetRecord.getExtbookIndexFromRefIndex(extRefIndex);
		SupBookRecord ebr = _externalBookBlocks[ebIx].getExternalBookRecord();
		if (!ebr.isExternalReferences()) {
			return null;
		}
		// Sheet name only applies if not a global reference
		int shIx = _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex);
		String usSheetName = null;
		if(shIx >= 0) {
		   usSheetName = ebr.getSheetNames()[shIx];
		}
		return new String[] {
				ebr.getURL(),
				usSheetName,
		};
	}


	/**
	 * @param extRefIndex as from a {@link Ref3DPtg} or {@link Area3DPtg}
	 * @return -1 if the reference is to an external book
	 */
	public int getIndexToInternalSheet(int extRefIndex) {
		return _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex);
	}

	
	public String resolveNameXText(int refIndex, int definedNameIndex) {
		int extBookIndex = _externSheetRecord.getExtbookIndexFromRefIndex(refIndex);
		return _externalBookBlocks[extBookIndex].getNameText(definedNameIndex);
	}

}
