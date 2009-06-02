package org.openstreetmap.josm.plugins.czechaddress;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Collection of utilities for manipulating strings.
 *
 * <p>This set of state-less utilities, which can be handy in all parts of
 * the plugin. Therefore all methods are {@code static} and the class is
 * {@code abstract}.</p>
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public abstract class StringUtils {

    /**
     * Returns a substring equivalent to <tt>^[0-9]*</tt> regexp match.
     *
     * @param s the input string
     * @return <tt>^[0-9]*</tt> substring match
     */
    public static String extractNumber(String s) {
        String result = "";
        for (int i=0; i<s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9')
                result += ch;
            else
                break;
        }
        return result;
    }

    public static String coordinateToString(double coor) {
        double degrees = Math.floor(coor);
        double minutes = Math.floor( 60*(coor-degrees) );
        double seconds = 60*60*(coor-degrees-minutes/60);

        return String.valueOf(Math.round(    degrees))     + "°" +
               String.valueOf(Math.round(    minutes))     + "'" +
               String.valueOf(Math.round(100*seconds)/100.0) + "\"";
    }

    public static String latLonToString(LatLon position) {
        if (position == null) return "";

        return "(lat: " + coordinateToString(position.lat())
             + " lon: " + coordinateToString(position.lon()) + ")";
    }

    /**
     * String matcher with abbreviations
     *
     * <p>Returns {@code true} even if s1="Nám. Svobody" and
     * s2="Náměstí Svobody".</p>
     */
    public static boolean matchAbbrev(String s1, String s2) {
        String[] parts1 = s1.split(" +");
        String[] parts2 = s2.split(" +");

        if (parts1.length != parts2.length)
            return false;

        for (int i=0; i<parts1.length; i++) {
            String part1 = parts1[i];
            String part2 = parts2[i];

            if (part1.charAt(part1.length()-1) == '.')
                part1 = part1.substring(0, part1.length()-1);

            if (part2.charAt(part2.length()-1) == '.')
                part2 = part2.substring(0, part2.length()-1);

            int minLen = Math.min(part1.length(), part2.length());
            part1 = part1.substring(0, minLen).toUpperCase();
            part2 = part2.substring(0, minLen).toUpperCase();

            if (!part1.equals(part2))
                return false;
        }
        return true;
    }

    /**
     * Capitalizes the given string (first letter of every word upper-case,
     * others lower-case). Czech grammar rules are more or less obeyed.
     *
     * <p><b>TODO:</b> This should be moved somewhere else.</p>
     *
     * @param s string to be capitalized
     * @return capitaized string
     */
    public static String capitalize(String s) {

        if (s == null)
            return s;

        String result = "";

        char last = ' ';
        for (char ch : s.toCharArray()) {
            if ((last >= 'a') && (last <= 'ž') ||
                (last >= 'A') && (last <= 'Ž'))
                ch = Character.toLowerCase(ch);
            else
                ch = Character.toTitleCase(ch);

            last = ch;
            result = result + ch;
        }

        String[] noCapitalize = { "Nad", "Pod", "U", "Na" };
        for (String noc : noCapitalize)
            result = result.replaceAll(" "+noc+" ", " "+noc.toLowerCase()+" ");

        return result;
    }
}
