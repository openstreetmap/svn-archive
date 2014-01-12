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

import java.util.Date;

import org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * <p>Convenience class representing a Summary Information stream in a
 * Microsoft Office document.</p>
 *
 * @author Rainer Klute <a
 *         href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @see DocumentSummaryInformation
 */
public final class SummaryInformation extends SpecialPropertySet {

    /**
     * <p>The document name a summary information stream usually has in a POIFS
     * filesystem.</p>
     */
    public static final String DEFAULT_STREAM_NAME = "\005SummaryInformation";

    public PropertyIDMap getPropertySetIDMap() {
    	return PropertyIDMap.getSummaryInformationProperties();
    }


    /**
     * <p>Creates a {@link SummaryInformation} from a given {@link
     * PropertySet}.</p>
     *
     * @param ps A property set which should be created from a summary
     *        information stream.
     * @throws UnexpectedPropertySetTypeException if <var>ps</var> does not
     *         contain a summary information stream.
     */
    public SummaryInformation(final PropertySet ps)
            throws UnexpectedPropertySetTypeException
    {
        super(ps);
        if (!isSummaryInformation())
            throw new UnexpectedPropertySetTypeException("Not a "
                    + getClass().getName());
    }

    /**
     * <p>Returns the title (or <code>null</code>).</p>
     *
     * @return The title or <code>null</code>
     */
    public String getTitle() // NO_UCD
    {
        return (String) getProperty(PropertyIDMap.PID_TITLE);
    }

    /**
     * <p>Returns the author (or <code>null</code>).</p>
     *
     * @return The author or <code>null</code>
     */
    public String getAuthor() // NO_UCD
    {
        return (String) getProperty(PropertyIDMap.PID_AUTHOR);
    }

    /**
     * <p>Returns the last author (or <code>null</code>).</p>
     *
     * @return The last author or <code>null</code>
     */
    public String getLastAuthor() // NO_UCD
    {
        return (String) getProperty(PropertyIDMap.PID_LASTAUTHOR);
    }

    /**
     * <p>Returns the revision number (or <code>null</code>). </p>
     *
     * @return The revision number or <code>null</code>
     */
    public String getRevNumber() // NO_UCD
    {
        return (String) getProperty(PropertyIDMap.PID_REVNUMBER);
    }

    /**
     * <p>Returns the creation time (or <code>null</code>).</p>
     *
     * @return The creation time or <code>null</code>
     */
    public Date getCreateDateTime() // NO_UCD
    {
        return (Date) getProperty(PropertyIDMap.PID_CREATE_DTM);
    }

    /**
     * <p>Returns the last save time (or <code>null</code>).</p>
     *
     * @return The last save time or <code>null</code>
     */
    public Date getLastSaveDateTime() // NO_UCD
    {
        return (Date) getProperty(PropertyIDMap.PID_LASTSAVE_DTM);
    }
}
