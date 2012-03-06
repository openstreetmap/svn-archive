/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.usermodel.HSSFErrorConstants;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public final class ErrorEval {


    // POI internal error codes
    private static final int CIRCULAR_REF_ERROR_CODE = 0xFFFFFFC4;
    private static final int FUNCTION_NOT_IMPLEMENTED_CODE = 0xFFFFFFE2;

    /**
     * Converts error codes to text.  Handles non-standard error codes OK.  
     * For debug/test purposes (and for formatting error messages).
     * @return the String representation of the specified Excel error code.
     */
    public static String getText(int errorCode) {
        if(HSSFErrorConstants.isValidCode(errorCode)) {
            return HSSFErrorConstants.getText(errorCode);
        }
        // It is desirable to make these (arbitrary) strings look clearly different from any other
        // value expression that might appear in a formula.  In addition these error strings should
        // look unlike the standard Excel errors.  Hence tilde ('~') was used.
        switch(errorCode) {
            case CIRCULAR_REF_ERROR_CODE: return "~CIRCULAR~REF~";
            case FUNCTION_NOT_IMPLEMENTED_CODE: return "~FUNCTION~NOT~IMPLEMENTED~";
        }
        return "~non~std~err(" + errorCode + ")~";
    }

    private int _errorCode;
    /**
     * @param errorCode an 8-bit value
     */
    private ErrorEval(int errorCode) {
        _errorCode = errorCode;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(getText(_errorCode));
        sb.append("]");
        return sb.toString();
    }
}
