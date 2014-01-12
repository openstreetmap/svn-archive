/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.dom;

import java.text.Format;
import java.text.SimpleDateFormat;

import org.jopendocument.util.FormatGroup;
import org.jopendocument.util.XMLDateFormat;

public class OOUtils {
    // as per 16.1 "Data Types" and 6.7.1 "Variable Value Types and Values"
    // see http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#isoformats

    // time means Duration for OpenDocument (see 6.7.1)
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("'PT'HH'H'mm'M'ss'S'");
    static public final Format DATE_FORMAT;
    static {
        // first date and time so we don't loose time information on format() or parse()
        // MAYBE add HH':'mm':'ss,SSS for OOo 1
        DATE_FORMAT = new FormatGroup(new XMLDateFormat(), new SimpleDateFormat("yyyy-MM-dd'T'HH':'mm':'ss"), new SimpleDateFormat("yyyy-MM-dd"));
    }
}