package org.openstreetmap.josm.plugins.czechaddress;

/**
 * Collection of utilities for manipulating strings.
 *
 * <p>This set of state-less utilities, which can be handy in all parts of
 * the plugin. Therefore all methods are {@code static} and the class is
 * {@code abstract}.</p>
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class StringUtils {

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

}
