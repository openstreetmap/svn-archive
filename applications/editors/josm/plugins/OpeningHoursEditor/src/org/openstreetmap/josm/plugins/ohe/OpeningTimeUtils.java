package org.openstreetmap.josm.plugins.ohe;

import java.util.ArrayList;

import org.openstreetmap.josm.plugins.ohe.gui.TimeRect;
import org.openstreetmap.josm.plugins.ohe.parser.OpeningTimeCompiler;

public class OpeningTimeUtils {
    // implements the subtraction of daytimes in spans of days when a day in
    // the list occurs direct afterwards
    public static ArrayList<int[]> convert(ArrayList<DateTime> dateTimes) {
        ArrayList<int[]> ret = new ArrayList<int[]>(); // the list which is
        // returned
        for (int i = 0; i < dateTimes.size(); ++i) { // iterate over every entry
            DateTime dateTime = dateTimes.get(i);
            ArrayList<DateTime> newDateTimes = new ArrayList<DateTime>();

            // test if the given entry is a single dayspan
            if (dateTime.daySpans.size() == 1 && dateTime.daySpans.get(0).isSpan()) {
                ArrayList<DaySpan> partDaySpans = new ArrayList<DaySpan>();
                int start_day = dateTime.daySpans.get(0).startDay;

                // look in every entry behind
                while (i + 1 < dateTimes.size()) {
                    ArrayList<DaySpan> following = dateTimes.get(i + 1).daySpans;
                    if (following.size() == 1 && following.get(0).startDay > dateTime.daySpans.get(0).startDay
                            && following.get(0).endDay < dateTime.daySpans.get(0).endDay) {
                        partDaySpans.add(new DaySpan(start_day, following.get(0).startDay - 1));
                        start_day = following.get(0).endDay + 1;
                        newDateTimes.add(dateTimes.get(i + 1));
                        i++;
                    } else {
                        break;
                    }
                }

                partDaySpans.add(new DaySpan(start_day, dateTime.daySpans.get(0).endDay));
                newDateTimes.add(new DateTime(partDaySpans, dateTime.daytimeSpans));
            }
            if (newDateTimes.isEmpty()) {
                newDateTimes.add(dateTime);
            }

            // create the int-array
            for (int j = 0; j < newDateTimes.size(); ++j) {
                DateTime dateTime2 = newDateTimes.get(j);
                for (DaySpan dayspan : dateTime2.daySpans) {
                    for (DaytimeSpan timespan : dateTime2.daytimeSpans) {
                        if (!timespan.isOff()) {
                            ret.add(new int[] { dayspan.startDay, dayspan.endDay, timespan.startMinute,
                                    timespan.endMinute });
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static class DaySpan {
        public int startDay;
        public int endDay;

        public DaySpan(int startDay, int endDay) {
            this.startDay = startDay;
            this.endDay = endDay;
        }

        public boolean isSpan() {
            return endDay > startDay;
        }

        public boolean isSingleDay() {
            return startDay == endDay;
        }
    }

    public static class DaytimeSpan {
        public int startMinute;
        public int endMinute;

        public DaytimeSpan(int startMinute, int endMinute) {
            this.startMinute = startMinute;
            this.endMinute = endMinute;
        }

        public boolean isOff() {
            return startMinute == -1;
        }

        public boolean isSpan() {
            return endMinute > startMinute;
        }
    }

    public static class DateTime {
        public ArrayList<DaySpan> daySpans;
        public ArrayList<DaytimeSpan> daytimeSpans;

        public DateTime(ArrayList<DaySpan> daySpans, ArrayList<DaytimeSpan> daytimeSpans) {
            this.daySpans = daySpans;
            this.daytimeSpans = daytimeSpans;
        }
    }

    // returns a String (e.g "Mo-Sa 10:00-20:00; Tu off") representing the
    // TimeRects
    public static String makeStringFromRects(ArrayList<TimeRect> givenTimeRects) {
        // create an array of booleans representing every minute on all the days
        // in a week
        boolean[][] minuteArray = new boolean[7][24 * 60 + 2];
        for (int day = 0; day < 7; ++day) {
            for (int minute = 0; minute < 24 * 60 + 2; ++minute) {
                minuteArray[day][minute] = false;
            }
        }
        for (TimeRect timeRect : givenTimeRects) {
            for (int day = timeRect.getDayStart(); day <= timeRect.getDayEnd(); ++day) {
                for (int minute = timeRect.getMinuteStart(); minute <= timeRect.getMinuteEnd(); ++minute) {
                    minuteArray[day][minute] = true;
                }
            }
        }

        String ret = "";
        int[] days = new int[7]; // an array representing the status of the days
        // 0 means nothing done with this day yet
        // 8 means the day is off
        // 0<x<8 means the day have the openinghours of day x
        // -8<x<0 means nothing done with this day yet, but it intersects a
        // range of days with same opening_hours
        for (int i = 0; i < 7; ++i) {
            String add = "";

            if (isArrayEmpty(minuteArray[i]) && days[i] == 0) {
                days[i] = 8;
            } else if (isArrayEmpty(minuteArray[i]) && days[i] < 0) {
                add = OpeningTimeCompiler.WEEKDAYS[i] + " off";
                days[i] = -8;
            } else if (days[i] <= 0) {
                days[i] = i + 1;
                int lastSameDay = i;
                int sameDayCount = 1;
                for (int j = i + 1; j < 7; ++j) {
                    if (arraysEqual(minuteArray[i], minuteArray[j])) {
                        days[j] = i + 1;
                        lastSameDay = j;
                        sameDayCount++;
                    }
                }
                if (sameDayCount == 1) {
                    // a single Day with this special opening_hours
                    add = OpeningTimeCompiler.WEEKDAYS[i] + " " + makeStringFromMinuteArray(minuteArray[i]);
                } else if (sameDayCount == 2) {
                    // exactly two Days with this special opening_hours
                    add = OpeningTimeCompiler.WEEKDAYS[i] + "," + OpeningTimeCompiler.WEEKDAYS[lastSameDay] + " "
                    + makeStringFromMinuteArray(minuteArray[i]);
                } else if (sameDayCount > 2) {
                    // more than two Days with this special opening_hours
                    add = OpeningTimeCompiler.WEEKDAYS[i] + "-" + OpeningTimeCompiler.WEEKDAYS[lastSameDay] + " "
                    + makeStringFromMinuteArray(minuteArray[i]);
                    for (int j = i + 1; j < lastSameDay; ++j) {
                        if (days[j] == 0) {
                            days[j] = -i - 1;
                        }
                    }
                }
            }

            if (!add.isEmpty()) {
                if (!ret.isEmpty()) {
                    ret += "; ";
                }
                ret += add;
            }
        }
        return ret;
    }

    // returns a String representing the openinghours on one special day (e.g.
    // "10:00-20:00")
    private static String makeStringFromMinuteArray(boolean[] minutes) {
        String ret = "";
        for (int i = 0; i < minutes.length; ++i) {
            if (minutes[i]) {
                int start = i;
                while (i < minutes.length && minutes[i]) {
                    ++i;
                }
                String addString = timeString(start);
                if (i - 1 == 24 * 60 + 1) {
                    addString += "+";
                } else if (start != i - 1) {
                    addString += "-" + timeString(i - 1);
                }
                if (!ret.isEmpty()) {
                    ret += ",";
                }
                ret += addString;
            }
        }
        return ret;
    }

    public static String timeString(int minutes) {
        return timeString(minutes, ClockSystem.TWENTYFOUR_HOURS);
    }

    public static String timeString(int minutes, ClockSystem hourMode) {
        return timeString(minutes, hourMode, false);
    }

    /**
     * @param minutes integer in range from 0 and 24*60 inclusive
     * @param hourMode 12 or 24 hour clock
     * @param showPeriod if 12 hour clock is chosen, the "AM"/"PM" will be shown
     * @return a formatted string of the time (for example "01:45 PM" or "13:45")
     */
    public static String timeString(int minutes, ClockSystem hourMode, boolean showPeriod) {
        int h = minutes / 60;
        String period = "";
        if (hourMode == ClockSystem.TWELVE_HOURS) {
            if (h == 24)
                return "midnight";
            else {
                if (showPeriod) {
                    period = h < 12 ? " AM" : " PM";
                }
                h %= 12;
                if (h == 0) {
                    h = 12;
                }
            }
        }
        int m = minutes % 60;
        return (h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m + period;
    }

    private static boolean isArrayEmpty(boolean[] bs) {
        for (int i = 0; i < bs.length; i++)
            if (bs[i])
                return false;
        return true;
    }

    private static boolean arraysEqual(boolean[] bs, boolean[] bs2) {
        boolean ret = true;
        for (int i = 0; i < bs.length; i++) {
            ret &= bs[i] == bs2[i];
        }
        return ret;
    }
}
