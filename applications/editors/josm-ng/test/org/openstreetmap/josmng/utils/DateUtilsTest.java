/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test for DateUtils.
 * 
 * @author nenik
 */
public class DateUtilsTest {

    public DateUtilsTest() {
    }

    public @Test void testFromStringUTC() {
        Date d = DateUtils.fromString("2001-02-03T04:05:06Z");
        checkDate(d, 2001, Calendar.FEBRUARY, 3, 4, 5, 6);
    }

    public @Test void testFromStringShifted() {
        Date d = DateUtils.fromString("2001-02-03T04:05:06+11:00");
        checkDate(d, 2001, Calendar.FEBRUARY, 2, 17, 5, 6);
        Date d2 = DateUtils.fromString("2001-02-03T04:05:06-11:00");
        checkDate(d2, 2001, Calendar.FEBRUARY, 3, 15, 5, 6);
    }

    public @Test void testFromStringExotic() {
        Date d = DateUtils.fromString("2001-02-03T04:05:06+11:30");
        checkDate(d, 2001, Calendar.FEBRUARY, 2, 16, 35, 6);
    }

    public @Test void testFromStringWithMillis() {
        Date d = DateUtils.fromString("2001-02-03T04:05:06.001Z");
        assertEquals("Millis field kept", 981173106001l, d.getTime());
    }

    public @Test void testFromStringZero() {
        Date d = DateUtils.fromString("1970-01-01T00:00:00Z");
        assertEquals("Epoch matches", 0l, d.getTime());
        checkDate(d, 1970, Calendar.JANUARY, 1, 0, 0, 0);
    }

    public @Test void testFromDate() {
        String s = DateUtils.fromDate(new Date(981173106000l));
        assertEquals("Right UTC formatting", "2001-02-03T04:05:06Z", s);

    }

    public @Test void testFromDateZero() {
        String s = DateUtils.fromDate(new Date(0l));
        assertEquals("Right UTC formatting", "1970-01-01T00:00:00Z", s);

    }
    
    private void checkDate(Date d, int year, int month, int day, int hour, int min, int sec) {
        GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        gc.setTime(d);
        
        assertEquals("Year matches", year, gc.get(Calendar.YEAR));
        assertEquals("Month matches", month, gc.get(Calendar.MONTH));
        assertEquals("Day matches", day, gc.get(Calendar.DAY_OF_MONTH));
        assertEquals("Hour matches", hour, gc.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minute matches", min, gc.get(Calendar.MINUTE));
        assertEquals("Second matches", sec, gc.get(Calendar.SECOND));
        assertEquals("no millis", 0, gc.get(Calendar.MILLISECOND));
    }
}
