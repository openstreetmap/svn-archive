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

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

/**
 * High level representation of a Excel workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 */
public interface Workbook {


    /**
     * Get the Sheet object at the given index.
     *
     * @param index of the sheet number (0-based physical & logical)
     * @return Sheet at the provided index
     */
    Sheet getSheetAt(int index);



	/**
	 * Retrieves the current policy on what to do when
	 *  getting missing or blank cells from a row.
     * <p>
	 * The default is to return blank and null cells.
	 *  {@link MissingCellPolicy}
     * </p>
	 */
	MissingCellPolicy getMissingCellPolicy();


}
