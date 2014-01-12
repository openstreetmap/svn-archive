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
package org.apache.poi.ss.format;

import java.util.Locale;

/**
 * This is the abstract supertype for the various cell formatters.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public abstract class CellFormatter {
    /** The original specified format. */
    protected final String format;

    /**
     * This is the locale used to get a consistent format result from which to
     * work.
     */
    public static final Locale LOCALE = Locale.US;

    /**
     * Creates a new formatter object, storing the format in {@link #format}.
     *
     * @param format The format.
     */
    public CellFormatter(String format) {
        this.format = format;
    }

    /**
     * Format a value according the format string.
     *
     * @param toAppendTo The buffer to append to.
     * @param value      The value to format.
     */
    public abstract void formatValue(StringBuffer toAppendTo, Object value);

    /**
     * Format a value according to the type, in the most basic way.
     *
     * @param toAppendTo The buffer to append to.
     * @param value      The value to format.
     */
    public abstract void simpleValue(StringBuffer toAppendTo, Object value);

}