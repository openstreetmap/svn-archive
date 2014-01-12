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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.hssf.record.formula.function.FunctionMetadata;
import org.apache.poi.hssf.record.formula.function.FunctionMetadataRegistry;


/**
 * This class provides the base functionality for Excel sheet functions
 * There are two kinds of function Ptgs - tFunc and tFuncVar
 * Therefore, this class will have ONLY two subclasses
 * @author  Avik Sengupta
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public abstract class AbstractFunctionPtg extends OperationPtg {

    /** All external functions have function index 255 */
    private static final short FUNCTION_INDEX_EXTERNAL = 255;

    private final byte returnClass;

    private final byte _numberOfArgs;
    private final short _functionIndex;

    protected AbstractFunctionPtg(int functionIndex, int pReturnClass, byte[] paramTypes, int nParams) {
        _numberOfArgs = (byte) nParams;
        _functionIndex = (short) functionIndex;
        returnClass = (byte) pReturnClass;
    }
    public final boolean isBaseToken() {
        return false;
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(lookupName(_functionIndex));
        sb.append(" nArgs=").append(_numberOfArgs);
        sb.append("]");
        return sb.toString();
    }

    public final short getFunctionIndex() {
        return _functionIndex;
    }
    public final int getNumberOfOperands() {
        return _numberOfArgs;
    }

    public final String getName() {
        return lookupName(_functionIndex);
    }
    /**
     * external functions get some special processing
     * @return <code>true</code> if this is an external function
     */
    public final boolean isExternalFunction() {
        return _functionIndex == FUNCTION_INDEX_EXTERNAL;
    }

    public final String toFormulaString() {
        return getName();
    }

    public String toFormulaString(String[] operands) {
        StringBuilder buf = new StringBuilder();

        if(isExternalFunction()) {
            buf.append(operands[0]); // first operand is actually the function name
            appendArgs(buf, 1, operands);
        } else {
            buf.append(getName());
            appendArgs(buf, 0, operands);
        }
        return buf.toString();
    }

    private static void appendArgs(StringBuilder buf, int firstArgIx, String[] operands) {
        buf.append('(');
        for (int i=firstArgIx;i<operands.length;i++) {
            if (i>firstArgIx) {
                buf.append(',');
            }
            buf.append(operands[i]);
        }
        buf.append(")");
    }

    public abstract int getSize();


    protected final String lookupName(short index) {
        if(index == FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL) {
            return "#external#";
        }
        FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByIndex(index);
        if(fm == null) {
            throw new RuntimeException("bad function index (" + index + ")");
        }
        return fm.getName();
    }


    public byte getDefaultOperandClass() {
        return returnClass;
    }

}
