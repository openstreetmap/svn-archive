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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Implementation of the cell range address lists,like is described
 * in OpenOffice.org's Excel Documentation: excelfileformat.pdf sec 2.5.14 -
 * 'Cell Range Address List'
 * 
 * In BIFF8 there is a common way to store absolute cell range address lists in
 * several records (not formulas). A cell range address list consists of a field
 * with the number of ranges and the list of the range addresses. Each cell
 * range address (called an ADDR structure) contains 4 16-bit-values.
 * </p>
 * 
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public class CellRangeAddressList {

	/**
	 * List of <tt>CellRangeAddress</tt>es. Each structure represents a cell range
	 */
	protected final List<CellRangeAddress> _list;

	public CellRangeAddressList() {
		_list = new ArrayList<CellRangeAddress>();
	}
	/**
	 * @param in the RecordInputstream to read the record from
	 */
	public CellRangeAddressList(RecordInputStream in) {
		this();
		int nItems = in.readUShort();

		for (int k = 0; k < nItems; k++) {
			_list.add(new CellRangeAddress(in));
		}
	}
	/**
	 * Get the number of following ADDR structures. The number of this
	 * structures is automatically set when reading an Excel file and/or
	 * increased when you manually add a new ADDR structure . This is the reason
	 * there isn't a set method for this field .
	 * 
	 * @return number of ADDR structures
	 */
	public int countRanges() {
		return _list.size();
	}

	/**
	 * @return <tt>CellRangeAddress</tt> at the given index
	 */
	public CellRangeAddress getCellRangeAddress(int index) {
		return _list.get(index);
	}

	public int getSize() {
		return getEncodedSize(_list.size());
	}
	/**
	 * @return the total size of for the specified number of ranges,
	 *  including the initial 2 byte range count
	 */
	public static int getEncodedSize(int numberOfRanges) {
		return 2 + CellRangeAddress.getEncodedSize(numberOfRanges);
	}

	public void serialize(LittleEndianOutput out) {
		int nItems = _list.size();
		out.writeShort(nItems);
		for (int k = 0; k < nItems; k++) {
			CellRangeAddress region = _list.get(k);
			region.serialize(out);
		}
	}
	
}
