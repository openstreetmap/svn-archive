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

package org.jopendocument.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Format a {@link Date} according to <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">W3C XML
 * Schema 1.0 Part 2, Section 3.2.7-14</a>.
 * 
 * @author Sylvain CUAZ
 * @see XMLGregorianCalendar
 */
@SuppressWarnings("serial")
public class XMLDateFormat extends Format {

    private final static DatatypeFactory factory;
    static {
        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            // shouldn't happen since an implementation is provided with the jre
            throw new IllegalStateException(e);
        }
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        final GregorianCalendar cal;
        if (obj instanceof GregorianCalendar)
            cal = (GregorianCalendar) obj;
        else {
            cal = new GregorianCalendar();
            cal.setTime((Date) obj);
        }
        return toAppendTo.append(factory.newXMLGregorianCalendar(cal).toXMLFormat());
    }

    @Override
    public Date parseObject(String source, ParsePosition pos) {
        try {
            final XMLGregorianCalendar res = factory.newXMLGregorianCalendar(source.substring(pos.getIndex()));
            pos.setIndex(source.length());
            return res.toGregorianCalendar().getTime();
        } catch (Exception e) {
            e.printStackTrace();
            pos.setErrorIndex(pos.getIndex());
            return null;
        }
    }
}
