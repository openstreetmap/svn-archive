/**
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

package org.openstreetmap.josm.plugins.elevation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * @author Oliver Provides several static methods to access way point
 *         attributes.
 */
public class WayPointHelper {
	/**
	 * The name of the elevation height of a way point.
	 */
	public static final String HEIGHT_ATTRIBUTE = "ele";

	private static GeoidCorrectionKind geoidKind = GeoidCorrectionKind.None;

	public static GeoidCorrectionKind getGeoidKind() {
		return geoidKind;
	}

	public static void setGeoidKind(GeoidCorrectionKind geoidKind) {
		WayPointHelper.geoidKind = geoidKind;
	}

	/**
	 * Gets the elevation (Z coordinate) of a JOSM way point.
	 * 
	 * @param wpt
	 *            The way point instance.
	 * @return The x coordinate or 0, if the given way point is null or contains
	 *         not height attribute.
	 */
	public static double getElevation(WayPoint wpt) {
		if (wpt != null) {
			if (!wpt.attr.containsKey(HEIGHT_ATTRIBUTE)) {
				return 0;
			}

			String height = wpt.getString(WayPointHelper.HEIGHT_ATTRIBUTE);
			try {
				double z = Double.parseDouble(height);

				if (geoidKind == GeoidCorrectionKind.Auto) {
					byte h = getGeoidCorrection(wpt);
					z += h;
				}
				return z;
			} catch (NumberFormatException e) {
				System.err.println(String.format(
						"Cannot parse double from '%s': %s", height, e
								.getMessage()));
				return 0;
			}
		} else {
			return 0;
		}
	}

	/*
	 * Gets the geoid height for the given way point. See also {@link
	 * GeoidData}.
	 */
	public static byte getGeoidCorrection(WayPoint wpt) {
		int lat = (int)Math.round(wpt.getCoor().lat());
		int lon = (int)Math.round(wpt.getCoor().lon());
		byte geoid = GeoidData.getGeoid(lat, lon);
		/*
		System.out.println(
				String.format("Geoid(%d, %d) = %d", lat, lon, geoid));
*/
		return geoid;
	}

	/**
	 * Reduces a given list of way points to the specified target size.
	 * 
	 * @param origList
	 *            The original list containing the way points.
	 * @param targetSize
	 *            The desired target size of the list. The resulting list may
	 *            contain fewer items, so targetSize should be considered as
	 *            maximum.
	 * @return A list containing the reduced list.
	 */
	public static List<WayPoint> downsampleWayPoints(List<WayPoint> origList,
			int targetSize) {
		if (origList == null)
			return null;
		if (targetSize <= 0)
			throw new IllegalArgumentException(
					"targetSize must be greater than zero");

		int origSize = origList.size();
		if (origSize <= targetSize) {
			return origList;
		}

		int delta = (int) Math.max(Math.ceil(origSize / targetSize), 2);

		List<WayPoint> res = new ArrayList<WayPoint>(targetSize);
		for (int i = 0; i < origSize; i += delta) {
			res.add(origList.get(i));
		}

		return res;
	}
	
	/**
	 * Gets the hour value of a way point in 24h format.
	 * @param wpt
	 * @return
	 */
	public static int getHourOfWayPoint(WayPoint wpt) {
		if (wpt == null) return -1;
		
		Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
		calendar.setTime(wpt.getTime());   // assigns calendar to given date 
		return calendar.get(Calendar.HOUR_OF_DAY); 
	}
	
	/**
	 * Gets the minute value of a way point in 24h format.
	 * @param wpt
	 * @return
	 */
	public static int getMinuteOfWayPoint(WayPoint wpt) {
		if (wpt == null) return -1;
		
		Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
		calendar.setTime(wpt.getTime());   // assigns calendar to given date 
		return calendar.get(Calendar.MINUTE); 
	}
}
