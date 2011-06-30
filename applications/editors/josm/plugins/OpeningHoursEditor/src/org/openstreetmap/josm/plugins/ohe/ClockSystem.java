package org.openstreetmap.josm.plugins.ohe;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

public enum ClockSystem {
    TWELVE_HOURS, TWENTYFOUR_HOURS;

    public static ClockSystem getClockSystem(Locale locale) {
        DateFormat stdFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        DateFormat localeFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
        String midnight = "";
        try {
            midnight = localeFormat.format(stdFormat.parse("12:00 AM"));
        } catch (ParseException ignore) {
        }
        if (midnight.contains("12"))
            return TWELVE_HOURS;
        else
            return TWENTYFOUR_HOURS;
    }
}