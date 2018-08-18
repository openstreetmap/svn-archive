// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ohe;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

import org.openstreetmap.josm.tools.Logging;

/**
 * Clock system (12 or 24 hours).
 */
public enum ClockSystem {
    
    /** 12-hour clock system */
    TWELVE_HOURS, 
    /** 24-hour clock system */
    TWENTYFOUR_HOURS;

    /**
     * Returns the clock system for the given locale.
     * @param locale The locale
     * @return the clock system for the given locale
     */
    public static ClockSystem getClockSystem(Locale locale) {
        DateFormat stdFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        DateFormat localeFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
        String midnight = "";
        try {
            midnight = localeFormat.format(stdFormat.parse("12:00 AM"));
        } catch (ParseException ignore) {
            Logging.trace(ignore);
        }
        if (midnight.contains("12"))
            return TWELVE_HOURS;
        else
            return TWENTYFOUR_HOURS;
    }
}
