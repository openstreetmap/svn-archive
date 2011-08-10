/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;

public class StringUtils {
	/**
	 * Checks, if a string is either null or empty.
	 *
	 * @param txt
	 *            Text to check
	 * @return True, if string is null or empty; otherwise false
	 */
	public static boolean isNullOrEmpty(String txt) {
		return txt == null || txt.length() == 0;
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
	 *         algorithm details please refer to {@link http
	 *         ://www.ics.uci.edu/~eppstein/161/960229.html}
	 */
	public static int lcsLength(String a, String b) {
		if (StringUtils.isNullOrEmpty(a))
			return 0;
		if (StringUtils.isNullOrEmpty(b))
			return 0;

		int[][] L = createLCSTable(a, b);
		return L[0][0];
	}

	/**
	 * Internal use only
	 */
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
				 */if (a.charAt(i) == b.charAt(j)) {
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
	 * @return
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

		l = null;
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
		if (haystack == null || haystack.size() == 0) {
			return null;
		}

		int lNeedle = needle.length();
		for (String curString : haystack) {
			int ll = lcsLength(needle, curString);
			double ratio = ll / (double)lNeedle;
			if (ratio > maxRatio) {
				maxRatio = ratio;
				bestMatch = curString;
			}

		}

		return bestMatch;
	}
}
