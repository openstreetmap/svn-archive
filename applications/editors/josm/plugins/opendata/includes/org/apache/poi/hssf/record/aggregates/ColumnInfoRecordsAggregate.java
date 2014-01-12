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

package org.apache.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.ColumnInfoRecord;

/**
 * @author Glen Stampoultzis
 */
public final class ColumnInfoRecordsAggregate extends RecordAggregate {
	/**
	 * List of {@link ColumnInfoRecord}s assumed to be in order
	 */
	private final List<ColumnInfoRecord> records;


	private static final class CIRComparator implements Comparator<ColumnInfoRecord> {
		public static final Comparator<ColumnInfoRecord> instance = new CIRComparator();
		private CIRComparator() {
			// enforce singleton
		}
		public int compare(ColumnInfoRecord a, ColumnInfoRecord b) {
			return compareColInfos(a, b);
		}
		public static int compareColInfos(ColumnInfoRecord a, ColumnInfoRecord b) {
			return a.getFirstColumn()-b.getFirstColumn();
		}
	}

	/**
	 * Creates an empty aggregate
	 */
	public ColumnInfoRecordsAggregate() {
		records = new ArrayList<ColumnInfoRecord>();
	}
	public ColumnInfoRecordsAggregate(RecordStream rs) {
		this();

		boolean isInOrder = true;
		ColumnInfoRecord cirPrev = null;
		while(rs.peekNextClass() == ColumnInfoRecord.class) {
			ColumnInfoRecord cir = (ColumnInfoRecord) rs.getNext();
			records.add(cir);
			if (cirPrev != null && CIRComparator.compareColInfos(cirPrev, cir) > 0) {
				isInOrder = false;
			}
			cirPrev = cir;
		}
		if (records.size() < 1) {
			throw new RuntimeException("No column info records found");
		}
		if (!isInOrder) {
			Collections.sort(records, CIRComparator.instance);
		}
	}

	/**
	 * Performs a deep clone of the record
	 */
	public Object clone() {
		ColumnInfoRecordsAggregate rec = new ColumnInfoRecordsAggregate();
		for (int k = 0; k < records.size(); k++) {
			ColumnInfoRecord ci = records.get(k);
			rec.records.add((ColumnInfoRecord) ci.clone());
		}
		return rec;
	}

	public void visitContainedRecords(RecordVisitor rv) {
		int nItems = records.size();
		if (nItems < 1) {
			return;
		}
		ColumnInfoRecord cirPrev = null;
		for(int i=0; i<nItems; i++) {
			ColumnInfoRecord cir = records.get(i);
			rv.visitRecord(cir);
			if (cirPrev != null && CIRComparator.compareColInfos(cirPrev, cir) > 0) {
				// Excel probably wouldn't mind, but there is much logic in this class
				// that assumes the column info records are kept in order
				throw new RuntimeException("Column info records are out of order");
			}
			cirPrev = cir;
		}
	}

	private ColumnInfoRecord getColInfo(int idx) {
		return records.get( idx );
	}

	/**
	 * Finds the <tt>ColumnInfoRecord</tt> which contains the specified columnIndex
	 * @param columnIndex index of the column (not the index of the ColumnInfoRecord)
	 * @return <code>null</code> if no column info found for the specified column
	 */
	public ColumnInfoRecord findColumnInfo(int columnIndex) {
		int nInfos = records.size();
		for(int i=0; i< nInfos; i++) {
			ColumnInfoRecord ci = getColInfo(i);
			if (ci.containsColumn(columnIndex)) {
				return ci;
			}
		}
		return null;
	}

}
