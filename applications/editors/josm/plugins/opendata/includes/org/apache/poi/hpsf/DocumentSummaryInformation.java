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

package org.apache.poi.hpsf;

import org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * <p>Convenience class representing a DocumentSummary Information stream in a
 * Microsoft Office document.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @author Drew Varner (Drew.Varner closeTo sc.edu)
 * @author robert_flaherty@hyperion.com
 * @see SummaryInformation
 */
public class DocumentSummaryInformation extends SpecialPropertySet
{

    /**
     * <p>The document name a document summary information stream
     * usually has in a POIFS filesystem.</p>
     */
    public static final String DEFAULT_STREAM_NAME =
        "\005DocumentSummaryInformation";

    public PropertyIDMap getPropertySetIDMap() {
    	return PropertyIDMap.getDocumentSummaryInformationProperties();
    }


    /**
     * <p>Creates a {@link DocumentSummaryInformation} from a given
     * {@link PropertySet}.</p>
     *
     * @param ps A property set which should be created from a
     * document summary information stream.
     * @throws UnexpectedPropertySetTypeException if <var>ps</var>
     * does not contain a document summary information stream.
     */
    public DocumentSummaryInformation(final PropertySet ps)
        throws UnexpectedPropertySetTypeException
    {
        super(ps);
        if (!isDocumentSummaryInformation())
            throw new UnexpectedPropertySetTypeException
                ("Not a " + getClass().getName());
    }
}
