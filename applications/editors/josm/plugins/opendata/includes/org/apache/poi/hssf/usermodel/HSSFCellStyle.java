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

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * High level representation of the style of a cell in a sheet of a workbook.
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
 * @see org.apache.poi.hssf.usermodel.HSSFCell#setCellStyle(HSSFCellStyle)
 */
public final class HSSFCellStyle implements CellStyle {
    private ExtendedFormatRecord _format                     = null;
    private short                _index                      = 0;
    private InternalWorkbook             _workbook                   = null;


    /** Creates new HSSFCellStyle why would you want to do this?? */
    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, HSSFWorkbook workbook)
    {
    	this(index, rec, workbook.getWorkbook());
    }
    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, InternalWorkbook workbook)
    {
        _workbook = workbook;
        _index = index;
        _format     = rec;
    }

    /**
     * get the index within the HSSFWorkbook (sequence within the collection of ExtnededFormat objects)
     * @return unique index number of the underlying record this style represents (probably you don't care
     *  unless you're comparing which one is which)
     */
    public short getIndex()
    {
        return _index;
    }

    /**
     * get the index of the format
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */

    public short getDataFormat()
    {
        return _format.getFormatIndex();
    }

    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the bound workbook
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     * @return the format string or "General" if not found
     */
    public String getDataFormatString() {
        return getDataFormatString(_workbook);
    }

    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the supplied low level workbook
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */
    public String getDataFormatString(org.apache.poi.hssf.model.InternalWorkbook workbook) {
    	HSSFDataFormat format = new HSSFDataFormat( workbook );

        return format.getFormat(getDataFormat());
    }

    /**
     * Verifies that this style belongs to the supplied Workbook.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     *  cell, to ensure the cell and the style are from the same
     *  workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToWorkbook(HSSFWorkbook wb) {
		if(wb.getWorkbook() != _workbook) {
			throw new IllegalArgumentException("This Style does not belong to the supplied Workbook. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
		}
	}



	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_format == null) ? 0 : _format.hashCode());
		result = prime * result + _index;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof HSSFCellStyle) {
			final HSSFCellStyle other = (HSSFCellStyle) obj;
			if (_format == null) {
				if (other._format != null)
					return false;
			} else if (!_format.equals(other._format))
				return false;
			if (_index != other._index)
				return false;
			return true;
		}
		return false;
	}
}
