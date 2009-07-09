package org.openstreetmap.josm.plugins.czechaddress;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
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
     * String matcher with abbreviations and regardless of diacritics.
     *
     * <p>Returns {@code true} even if s1="Nam. Svobody" and
     * s2="Náměstí Svobody".</p>
     */
    public static boolean matchAbbrev(String s1, String s2) {

        s1 = anglicize(s1);
        s2 = anglicize(s2);

        List<Integer> beg1 = new ArrayList<Integer>(4);
        List<Integer> beg2 = new ArrayList<Integer>(4);

        char lastChar = ' ';
        for (int i=0; i<s1.length(); i++) {
            if (s1.charAt(i) != ' ' && lastChar == ' ')
                beg1.add(i);
            lastChar = s1.charAt(i);
        }

        lastChar = ' ';
        for (int i=0; i<s2.length(); i++) {
            if (s2.charAt(i) != ' ' && lastChar == ' ')
                beg2.add(i);
            lastChar = s2.charAt(i);
        }

        if (beg1.size() != beg2.size())
            return false;

        for (int i=0; i<beg1.size(); i++) {

            int pos1 = beg1.get(i);
            int pos2 = beg2.get(i);

            boolean doContinue = false;
            while (pos1 < s1.length() && pos2 < s2.length()) {
                if (s1.charAt(pos1) == '.' || s2.charAt(pos2) == '.')
                    {doContinue = true; break;}
                if (s1.charAt(pos1) == ' ' && s2.charAt(pos2) == ' ')
                     {doContinue = true; break;}

                if (Character.toUpperCase(s1.charAt(pos1)) !=
                    Character.toUpperCase(s2.charAt(pos2)))
                    return false;

                pos1++;
                pos2++;
            }
            if (doContinue) continue;

            if (pos1 >= s1.length() ^ pos2 >= s2.length())
                return false;
        }
        
        return true;
    }

    /**
     * Capitalizes the given string (first letter of every word upper-case,
     * others lower-case). Czech grammar rules are more or less obeyed.
     *
     * @param s string to be capitalized
     * @return capitaized string
     */
    public static String capitalize(String s) {
        if (s == null) return null;

        char[] charr = s.toCharArray();
        char last = ' ';
        char ch = last;
        for (int i=0; i<charr.length; i++) {
            ch = charr[i];
            if ((last >= 'a') && (last <= 'ž') ||
                (last >= 'A') && (last <= 'Ž'))
                ch = Character.toLowerCase(ch);
            else
                ch = Character.toTitleCase(ch);

            last = charr[i] = ch;
        }
        String result = String.valueOf(charr);

        result = result.replaceAll("Nábř. ", "nábřeží ");
        result = result.replaceAll("Ul. ",   "ulice ");
        result = result.replaceAll("Nám. ",  "náměstí ");
        result = result.replaceAll("Kpt. ",  "kapitána ");
        result = result.replaceAll("Bří. ",  "bratří ");

        String[] noCapitalize = { "Nad", "Pod", "U", "Na", "Z" };
        for (String noc : noCapitalize)
            result = result.replaceAll(" "+noc+" ", " "+noc.toLowerCase()+" ");


        String[] mesice = {"Ledna", "Února", "Března", "Dubna", "Května",
            "Máje", "Června", "Července", "Srpna", "Září", "Října",
            "Listopadu", "Prosince"};
        for (String mesic : mesice)
            result = result.replaceAll("."+mesic, ". " + mesic.toLowerCase());


        String[] noBegCap = {"Třída", "Ulice", "Náměstí", "Nábřeží"};
        for (String noc : noBegCap)
            result = result.replaceAll(noc, noc.toLowerCase());

        return result.replaceAll("  ", " ");
    }

    /**
     * Remove diacritics from the string.
     *
     * <p>This method was posted on the
     * <a href='http://forums.sun.com/thread.jspa?messageID=10190825#10190825'>
     * SUN forum</a> by
     * <a href='http://forums.sun.com/profile.jspa?userID=43408'>
     * <i>Alan Moore</i></a>.</p>
     */
    public static String anglicize(String str) {
        String strNFD = Normalizer.normalize(str, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(str.length());
        for (char ch : strNFD.toCharArray()) {
            if (Character.getType(ch) != Character.NON_SPACING_MARK) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
