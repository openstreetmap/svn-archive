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

package org.apache.poi.hpsf.wellknown;


/**
 * <p>Maps section format IDs to {@link PropertyIDMap}s. It is
 * initialized with two well-known section format IDs: those of the
 * <tt>\005SummaryInformation</tt> stream and the
 * <tt>\005DocumentSummaryInformation</tt> stream.</p>
 *
 * <p>If you have a section format ID you can use it as a key to query
 * this map.  If you get a {@link PropertyIDMap} returned your section
 * is well-known and you can query the {@link PropertyIDMap} for PID
 * strings. If you get back <code>null</code> you are on your own.</p>
 *
 * <p>This {@link java.util.Map} expects the byte arrays of section format IDs
 * as keys. A key maps to a {@link PropertyIDMap} describing the
 * property IDs in sections with the specified section format ID.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
public class SectionIDMap {

    /**
     * <p>The SummaryInformation's section's format ID.</p>
     */
    public static final byte[] SUMMARY_INFORMATION_ID = new byte[]
    {
        (byte) 0xF2, (byte) 0x9F, (byte) 0x85, (byte) 0xE0,
        (byte) 0x4F, (byte) 0xF9, (byte) 0x10, (byte) 0x68,
        (byte) 0xAB, (byte) 0x91, (byte) 0x08, (byte) 0x00,
        (byte) 0x2B, (byte) 0x27, (byte) 0xB3, (byte) 0xD9
    };

    /**
     * <p>The DocumentSummaryInformation's first and second sections' format
     * ID.</p>
     */
    public static final byte[][] DOCUMENT_SUMMARY_INFORMATION_ID = new byte[][]
    {
        {
            (byte) 0xD5, (byte) 0xCD, (byte) 0xD5, (byte) 0x02,
            (byte) 0x2E, (byte) 0x9C, (byte) 0x10, (byte) 0x1B,
            (byte) 0x93, (byte) 0x97, (byte) 0x08, (byte) 0x00,
            (byte) 0x2B, (byte) 0x2C, (byte) 0xF9, (byte) 0xAE
        },
        {
            (byte) 0xD5, (byte) 0xCD, (byte) 0xD5, (byte) 0x05,
            (byte) 0x2E, (byte) 0x9C, (byte) 0x10, (byte) 0x1B,
            (byte) 0x93, (byte) 0x97, (byte) 0x08, (byte) 0x00,
            (byte) 0x2B, (byte) 0x2C, (byte) 0xF9, (byte) 0xAE
        }
    };
}
