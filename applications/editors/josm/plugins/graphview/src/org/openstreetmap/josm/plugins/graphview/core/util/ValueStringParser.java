package org.openstreetmap.josm.plugins.graphview.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValueStringParser {

	/** prevents instantiation */
	private ValueStringParser() { }

	/** pattern that splits into a part before and after a decimal point */
	private static final Pattern DEC_POINT_PATTERN = Pattern.compile("^(\\-?\\d+)\\.(\\d+)$");

	public static final Float parseOsmDecimal(String value, boolean allowNegative) {

		/* positive integer */

		try {

			int weight = Integer.parseInt(value);
			if (weight >= 0 || allowNegative) {
				return (float)weight;
			}

		} catch (NumberFormatException nfe) {}

		/* positive number with decimal point */

		Matcher matcher = DEC_POINT_PATTERN.matcher(value);

		if (matcher.matches()) {

			String stringBeforePoint = matcher.group(1);
			String stringAfterPoint = matcher.group(2);

			if (stringBeforePoint.length() > 0 || stringAfterPoint.length() > 0) {

				try {

					float beforePoint = Integer.parseInt(stringBeforePoint);
					float afterPoint = Integer.parseInt(stringAfterPoint);

					double result = Math.signum(beforePoint) *
					(Math.abs(beforePoint)
							+ Math.pow(10, -stringAfterPoint.length()) * afterPoint);

					if (result >= 0 || allowNegative) {
						return (float)result;
					}

				} catch (NumberFormatException nfe) {}

			}
		}

		return null;
	}

	private static final Pattern KMH_PATTERN = Pattern.compile("^(\\d+)\\s*km/h$");
	private static final Pattern MPH_PATTERN = Pattern.compile("^(\\d+)\\s*mph$");

	private static final float KM_PER_MILE = 1.609344f;

	/**
	 * parses a speed value given e.g. for the "maxspeed" key.
	 *
	 * @return  speed in km/h; null if value had syntax errors
	 */
	public static final Float parseSpeed(String value) {

		/* try numeric speed (implied km/h) */

		Float maxspeed = parseOsmDecimal(value, false);
		if (maxspeed != null) {
			return maxspeed;
		}

		/* try km/h speed */

		Matcher kmhMatcher = KMH_PATTERN.matcher(value);
		if (kmhMatcher.matches()) {
			String kmhString = kmhMatcher.group(1);
			try {
				return (float)Integer.parseInt(kmhString);
			} catch (NumberFormatException nfe) {}
		}

		/* try mph speed */

		Matcher mphMatcher = MPH_PATTERN.matcher(value);
		if (mphMatcher.matches()) {
			String mphString = mphMatcher.group(1);
			try {
				int mph = Integer.parseInt(mphString);
				return KM_PER_MILE * mph;
			} catch (NumberFormatException nfe) {}
		}

		/* all possibilities failed */

		return null;
	}

	private static final Pattern INCLINE_PATTERN = Pattern.compile("^(\\-?\\d+(?:\\.\\d+)?)\\s*%$");

	/**
	 * parses an incline value as given for the "incline" key.
	 *
	 * @return  incline in percents; null if value had syntax errors
	 */
	public static final Float parseIncline(String value) {

		Matcher inclineMatcher = INCLINE_PATTERN.matcher(value);
		if (inclineMatcher.matches()) {
			String inclineString = inclineMatcher.group(1);
			return parseOsmDecimal(inclineString, true);
		}

		return null;
	}
}
