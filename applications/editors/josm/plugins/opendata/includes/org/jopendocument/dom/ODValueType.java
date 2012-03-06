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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A type of value, as per 16.1 "Data Types" and 6.7.1 "Variable Value Types and Values"
 */
public enum ODValueType {

    /**
     * Parses to {@link BigDecimal} to return the exact number.
     */
    FLOAT("value", Number.class) {

        @Override
        public String format(Object o) {
            // avoid 1.23E+3
            if (o instanceof BigDecimal)
                return ((BigDecimal) o).toPlainString();
            else
                return ((Number) o).toString();
        }

        @Override
        public BigDecimal parse(String s) {
            return new BigDecimal(s);
        }

    },
    PERCENTAGE("value", Number.class) {

        @Override
        public String format(Object o) {
            return FLOAT.format(o);
        }

        @Override
        public Object parse(String s) {
            return FLOAT.parse(s);
        }

    },
    CURRENCY("value", Number.class) {

        @Override
        public String format(Object o) {
            return FLOAT.format(o);
        }

        @Override
        public Object parse(String s) {
            return FLOAT.parse(s);
        }
    },
    DATE("date-value", Date.class, Calendar.class) {

        @Override
        public String format(Object o) {
            final Date d = o instanceof Calendar ? ((Calendar) o).getTime() : (Date) o;
            return OOUtils.DATE_FORMAT.format(d);
        }

        @Override
        public Date parse(String date) {
            if (date.length() == 0)
                return null;
            else {
                try {
                    return (Date) OOUtils.DATE_FORMAT.parseObject(date);
                } catch (ParseException e) {
                    throw new IllegalStateException("wrong date: " + date, e);
                }
            }
        }

    },
    TIME("time-value", Calendar.class) {
        // FIXME
        @Override
        public String format(Object o) {
            final Calendar cal = (Calendar) o;
            // adjust the format TZ to the calendar's
            // that way even you pass a non default Calendar, if you did
            // myCal.set(HOUR_OF_DAY, 22), the string will have "22H"
            final SimpleDateFormat fmt = (SimpleDateFormat) OOUtils.TIME_FORMAT.clone();
            fmt.setTimeZone(cal.getTimeZone());
            return fmt.format(cal.getTime());
        }

        @Override
        public Calendar parse(String date) {
            if (date.length() == 0)
                return null;
            else {
                // take the calendar from the format, that way if date contains "22H"
                // returnedCal.get(HOUR_OF_DAY) will return 22 (even if TIME_FORMAT wasn't set to
                // the default TZ)
                final Calendar cal = (Calendar) OOUtils.TIME_FORMAT.getCalendar().clone();
                try {
                    cal.setTime((Date) OOUtils.TIME_FORMAT.parseObject(date));
                } catch (ParseException e) {
                    throw new IllegalStateException("wrong date: " + date, e);
                }
                return cal;
            }
        }

    },
    BOOLEAN("boolean-value", Boolean.class) {

        @Override
        public String format(Object o) {
            return ((Boolean) o).toString().toLowerCase();
        }

        @Override
        public Boolean parse(String s) {
            return Boolean.valueOf(s);
        }

    },
    STRING("string-value", String.class) {

        @Override
        public String format(Object o) {
            return o.toString();
        }

        @Override
        public String parse(String s) {
            return s;
        }
    };

    private final String attr;
    private final List<Class<?>> acceptedClasses;

    private ODValueType(String attr, Class<?>... classes) {
        this.attr = attr;
        this.acceptedClasses = Arrays.asList(classes);
    }

    /**
     * The name of the value attribute for this value type.
     * 
     * @return the value attribute, eg "boolean-value".
     */
    public final String getValueAttribute() {
        return this.attr;
    }

    public boolean canFormat(Class<?> toFormat) {
        for (final Class<?> c : this.acceptedClasses)
            if (c.isAssignableFrom(toFormat))
                return true;
        return false;
    }

    public abstract String format(Object o);

    public abstract Object parse(String s);

    /**
     * The value for the value-type attribute.
     * 
     * @return the value for the value-type attribute, eg "float".
     */
    public final String getName() {
        return this.name().toLowerCase();
    }

    /**
     * The instance for the passed value type.
     * 
     * @param name the value of the value-type attribute, eg "date".
     * @return the corresponding instance, eg {@link #DATE}.
     */
    public static ODValueType get(String name) {
        return ODValueType.valueOf(name.toUpperCase());
    }

    /**
     * Try to guess the value type for the passed object.
     * 
     * @param o the object.
     * @return a value type capable of formatting <code>o</code> or <code>null</code>.
     */
    public static ODValueType forObject(Object o) {
        if (o instanceof Number)
            return FLOAT;
        else if (o instanceof Boolean)
            return BOOLEAN;
        else if (o instanceof String)
            return STRING;
        else if (o instanceof Calendar && !((Calendar) o).isSet(Calendar.DATE))
            return TIME;
        else if (DATE.canFormat(o.getClass()))
            return DATE;
        else
            return null;
    }

}