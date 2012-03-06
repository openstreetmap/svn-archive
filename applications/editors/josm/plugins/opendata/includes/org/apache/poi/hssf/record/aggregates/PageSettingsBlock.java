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

import org.apache.poi.hssf.record.HorizontalPageBreakRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.VerticalPageBreakRecord;

/**
 * Groups the page settings records for a worksheet.<p/>
 *
 * See OOO excelfileformat.pdf sec 4.4 'Page Settings Block'
 *
 * @author Josh Micich
 */
public final class PageSettingsBlock {

	/**
	 * @return <code>true</code> if the specified Record sid is one belonging to the
	 * 'Page Settings Block'.
	 */
	public static boolean isComponentRecord(int sid) {
		switch (sid) {
			case HorizontalPageBreakRecord.sid:
			case VerticalPageBreakRecord.sid:
			case UnknownRecord.PLS_004D:
			case UnknownRecord.BITMAP_00E9:
			case UnknownRecord.PRINTSIZE_0033:
				return true;
		}
		return false;
	}

}
