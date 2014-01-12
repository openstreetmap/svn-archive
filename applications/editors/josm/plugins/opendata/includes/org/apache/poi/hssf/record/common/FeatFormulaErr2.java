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

package org.apache.poi.hssf.record.common;

import org.apache.poi.hssf.record.FeatRecord;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: FeatFormulaErr2 (Formula Evaluation Shared Feature) common record part
 * <P>
 * This record part specifies Formula Evaluation & Error Ignoring data 
 *  for a sheet, stored as part of a Shared Feature. It can be found in 
 *  records such as {@link FeatRecord}.
 * For the full meanings of the flags, see pages 669 and 670
 *  of the Excel binary file format documentation.
 */
public final class FeatFormulaErr2 implements SharedFeature {

	/**
	 * What errors we should ignore
	 */
	private int errorCheck;
	
	
	public FeatFormulaErr2() {}

	public FeatFormulaErr2(RecordInputStream in) {
		errorCheck = in.readInt();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" [FEATURE FORMULA ERRORS]\n");
		buffer.append("  checkCalculationErrors    = "); 
		buffer.append("  checkEmptyCellRef         = "); 
		buffer.append("  checkNumbersAsText        = "); 
		buffer.append("  checkInconsistentRanges   = "); 
		buffer.append("  checkInconsistentFormulas = "); 
		buffer.append("  checkDateTimeFormats      = "); 
		buffer.append("  checkUnprotectedFormulas  = "); 
		buffer.append("  performDataValidation     = "); 
		buffer.append(" [/FEATURE FORMULA ERRORS]\n");
		return buffer.toString();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeInt(errorCheck);
	}

	public int getDataSize() {
		return 4;
	}
	

}
