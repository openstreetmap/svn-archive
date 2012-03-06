/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util.logging;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Wraps a {@link Format} object in order to either parse fully a string, or log a warning.
 * This class provides a {@link #parse} method which performs the following tasks:
 * <p>
 * <ul>
 *   <li>Checks if the string was fully parsed and log a warning if it was not. This is
 *       different than the default {@link #parseObject(String)} behavior which check only
 *       if the <em>begining</em> of the string was parsed and ignore any remaining characters.</li>
 *   <li>Ensures that the parsed object is of some specific class specified at construction time.</li>
 *   <li>If the string can't be fully parsed or is not of the expected class, logs a warning.</li>
 * </ul>
 *
 * @since 2.4
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/util/logging/LoggedFormat.java $
 * @version $Id: LoggedFormat.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux
 */
public class LoggedFormat {

    /**
     * Formats an error message for an unparsable string. This method performs the same work that
     * {@link #formatUnparsable(String, int, int, Locale, Level) formatUnparsable(..., Level)},
     * except that the result is returned as a {@link String} rather than a {@link LogRecord}.
     * This is provided as a convenience method for creating the message to give to an
     * {@linkplain Exception#Exception(String) exception constructor}.
     *
     * @param  text The unparsable string.
     * @param  index The parse position. This is usually {@link ParsePosition#getIndex}.
     * @param  errorIndex The index where the error occured. This is usually
     *         {@link ParsePosition#getErrorIndex}.
     * @param  locale The locale for the message, or {@code null} for the default one.
     * @return A formatted error message.
     *
     * @since 2.5
     */
    public static String formatUnparsable(final String text, final int index,
            final int errorIndex, final Locale locale)
    {
        return (String) doFormatUnparsable(text, index, errorIndex, locale, null);
    }

    /**
     * Implementation of {@code formatUnparsable} methods. Returns a {@link LogRecord}
     * if {@code level} is non-null, or a {@link String} otherwise.
     */
    private static Object doFormatUnparsable(String text, final int index, int errorIndex,
                                             final Locale locale, final Level level)
    {
        final Errors resources = Errors.getResources(locale);
        final int length = text.length();
        if (errorIndex < index) {
            errorIndex = index;
        }
        if (errorIndex == length) {
            if (level != null) {
                return resources.getLogRecord(level, ErrorKeys.UNEXPECTED_END_OF_STRING);
            }
            return resources.getString(ErrorKeys.UNEXPECTED_END_OF_STRING);
        }
        int upper = errorIndex;
        if (upper < length) {
            final int type = Character.getType(text.charAt(upper));
            while (++upper < length) {
                if (Character.getType(text.charAt(upper)) != type) {
                    break;
                }
            }
        }
        final String error = text.substring(errorIndex, upper);
        text = text.substring(index);
        if (level != null) {
            return resources.getLogRecord(level, ErrorKeys.UNPARSABLE_STRING_$2, text, error);
        }
        return resources.getString(ErrorKeys.UNPARSABLE_STRING_$2, text, error);
    }
}
