// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;

/**
 * Various String utilities.
 */
public final class StringUtils {

    private StringUtils() {
        // Hide default constructor for utilities classes
    }

    /**
     * Checks, if a string is either null or empty.
     *
     * @param txt
     *            Text to check
     * @return True, if string is null or empty; otherwise false
     */
    public static boolean isNullOrEmpty(String txt) {
        return txt == null || txt.isEmpty();
    }

    /**
     * Gets the length of the longest common substring of a and b
     *
     * @param a
     *            First string
     * @param b
     *            Second string
     * @return The length of the longest common substring or 0, if no common
     *         sequence exists or one of the arguments are invalid. For
     *         algorithm details please refer to
     *         <a href="https://www.ics.uci.edu/~eppstein/161/960229.html">ICS 161: Design and Analysis of Algorithms</a>
     */
    public static int lcsLength(String a, String b) {
        if (StringUtils.isNullOrEmpty(a))
            return 0;
        if (StringUtils.isNullOrEmpty(b))
            return 0;

        int[][] L = createLCSTable(a, b);
        return L[0][0];
    }

    private static int[][] createLCSTable(String a, String b) {
        if (StringUtils.isNullOrEmpty(a))
            return null;
        if (StringUtils.isNullOrEmpty(b))
            return null;

        int m = a.length();
        int n = b.length();
        int[][] l = new int[m + 1][n + 1];
        for (int i = 0; i < l.length; i++) {
            l[i] = new int[n + 1];
        }

        int i, j;
        for (i = m - 1; i >= 0; i--) {
            for (j = n - 1; j >= 0; j--) {
                /*
                 * if (i >= m || j >= n) { l[i][j] = 0; } else
                 */
                if (a.charAt(i) == b.charAt(j)) {
                    l[i][j] = 1 + l[i + 1][j + 1];
                } else {
                    l[i][j] = Math.max(l[i + 1][j], l[i][j + 1]);
                }
            }
        }
        return l;
    }

    /**
     * Gets the longest common substring of a and b.
     *
     * @param a The first string.
     * @param b The second string.
     * @return the longest common substring of a and b
     */
    public static String getLongestCommonSubstring(String a, String b) {
        if (StringUtils.isNullOrEmpty(a))
            return null;
        if (StringUtils.isNullOrEmpty(b))
            return null;

        StringBuffer sb = new StringBuffer();
        int[][] l = createLCSTable(a, b);
        int m = a.length();
        int n = b.length();
        int i = 0;
        int j = 0;
        while (i < m && j < n) {
            char aa = a.charAt(i);
            char bb = b.charAt(j);
            if (aa == bb) {
                sb.append(aa);
                i++;
                j++;
            } else if (l[i + 1][j] >= l[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }

        return sb.toString();
    }

    /**
     * @param needle The string to find the best match for.
     * @param haystack The list of strings to pick the best match from.
     * @return The string of the list with the longest common substring to needle or
     * <tt>null</tt>, if either <tt>needle</tt> or <tt>haystack</tt> is empty or null.
     */
    public static String findBestMatch(String needle, List<String> haystack) {
        String bestMatch = null;
        double maxRatio = Double.MIN_VALUE;

        if (StringUtils.isNullOrEmpty(needle)) {
            return null;
        }
        if (haystack == null || haystack.isEmpty()) {
            return null;
        }

        int lNeedle = needle.length();
        for (String curString : haystack) {
            int ll = lcsLength(needle, curString);
            double ratio = ll / (double) lNeedle;
            if (ratio > maxRatio) {
                maxRatio = ratio;
                bestMatch = curString;
            }
        }

        return bestMatch;
    }
}
