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
import java.util.Locale;

import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * Provides methods to access way point attributes and some utility methods regarding elevation stuff (
 * e. g. special text formats, unit conversion, geoid calc).
 * @author Oliver Wieland <oliver.wieland@online.de> 
 */
public class WayPointHelper {
	public static double METER_TO_FEET = 3.280948;
	
	/* Countries which use the imperial system instead of the metric system. */
	private static String IMPERIAL_SYSTEM_COUNTRIES[] = {
		"en_US", 	/* USA */
		"en_CA",	/* Canada */
		"en_AU",	/* Australia */
		"en_NZ",	/* New Zealand */
//		"de_DE",	/* for testing only */
		"en_ZA"	/* South Africa */
	};
	
	/**
	 * The name of the elevation height of a way point.
	 */
	public static final String HEIGHT_ATTRIBUTE = "ele";
	
	private static UnitMode unitMode = UnitMode.NotSelected;
		
	private static GeoidCorrectionKind geoidKind = GeoidCorrectionKind.None;

	/**
	 * Gets the current mode of GEOID correction.
	 * @return
	 */
	public static GeoidCorrectionKind getGeoidKind() {
		return geoidKind;
	}

	public static void setGeoidKind(GeoidCorrectionKind geoidKind) {
		WayPointHelper.geoidKind = geoidKind;
	}
	
	/**
	 * Gets the current unit mode (metric or imperial).
	 * @return
	 */
	public static UnitMode getUnitMode() {
		//TODO: Use this until /JOSM/src/org/openstreetmap/josm/gui/NavigatableComponent.java 
		// has a an appropriate method 
		
		// unit mode already determined?
		if (unitMode != UnitMode.NotSelected) {
			return unitMode;
		}
		
		// Set default
		unitMode = UnitMode.Metric;
		
		// Check if user could prefer imperial system 
		Locale l = Locale.getDefault();		
		for (int i = 0; i < IMPERIAL_SYSTEM_COUNTRIES.length; i++) {
			String ctry = l.toString();
			if (IMPERIAL_SYSTEM_COUNTRIES[i].equals(ctry)) {
				unitMode = UnitMode.Imperial;
			}
		}
		
		return unitMode;
	}
	
	/**
	 * Gets the unit string for elevation ("m" or "ft").
	 * @return
	 */
	public static String getUnit() {		
		switch (getUnitMode()) {
		case Metric:
			return "m";
		case Imperial:
			return "ft";
		default:
			throw new RuntimeException("Invalid or unsupported unit mode: " + unitMode);
		}
	}
	
	/**
	 * Gets the elevation (Z coordinate) of a GPX way point in meter or feet (for
	 * US, UK, ZA, AU, NZ and CA). 
	 * 
	 * @param wpt
	 *            The way point instance.
	 * @return The x coordinate or 0, if the given way point is null or contains
	 *         not height attribute.
	 */
	public static double getElevation(WayPoint wpt) {
		double ele = getInternalElevation(wpt);

		if (getUnitMode() == UnitMode.Imperial) {
				// translate to feet
		    return meter2Feet(ele);
		}	
		
		return ele;
	}
	
	/**
	 * Computes the slope <b>in percent</b> between two way points. E. g. an elevation gain of 12m 
	 * within a distance of 100m is equal to a slope of 12%. 
	 *
	 * @param w1 the first way point
	 * @param w2 the second way point
	 * @return the slope in percent
	 */
	public static double computeSlope(WayPoint w1, WayPoint w2) {
	    	// same coordinates? -> return 0, if yes
	    	if (w1.getCoor().equals(w2.getCoor())) return 0;
	    
		// get distance in meters and divide it by 100 in advance
		double distInMeter = w1.getCoor().greatCircleDistance(w2.getCoor()) / 100.0;
		
		// convert to feet?
		if (getUnitMode() == UnitMode.Imperial) {
		    distInMeter = meter2Feet(distInMeter);
		}
				
		// get elevation (difference) - is converted automatically to feet
		int ele1 = (int) WayPointHelper.getElevation(w1);
		int ele2 = (int) WayPointHelper.getElevation(w2);
		int dH = ele2 - ele1;
		
		// Slope in percent is define as elevation gain/loss in meters related to a distance of 100m
		return dH / distInMeter;
	}
	
	/**
	 * Converts meter into feet
	 *
	 * @param meter the meter
	 * @return the double
	 */
	public static double meter2Feet(double meter) {
	    return meter * METER_TO_FEET;
	}
	
	/**
	 * Gets the elevation string for a given elevation, e. g "300m" or "800ft".
	 * @param elevation
	 * @return
	 */
	public static String getElevationText(int elevation) {
		return String.format("%d %s", elevation, getUnit());
	}
	
	/**
	 * Gets the elevation string for a given elevation, e. g "300m" or "800ft".
	 * @param elevation
	 * @return
	 */
	public static String getElevationText(double elevation) {
		return String.format("%d %s", (int)Math.round(elevation), getUnit());
	}
	
	/**
	 * Gets the elevation string for a given way point, e. g "300m" or "800ft".
	 * @param elevation
	 * @return
	 */
	public static String getElevationText(WayPoint wpt) {
		if (wpt == null) return null;
		
		int elevation = (int)Math.round(WayPointHelper.getElevation(wpt));
		return String.format("%d %s", (int)elevation, getUnit());
	}
	
	/**
	 * Get the time string for a given way point.
	 * @param wpt
	 * @return
	 */
	public static String getTimeText(WayPoint wpt) {
		if (wpt == null) return null; 
		
		int hour = WayPointHelper.getHourOfWayPoint(wpt);
		int min = WayPointHelper.getMinuteOfWayPoint(wpt);		
		return String.format("%02d:%02d", hour, min);
	}

	/**
	 * Gets the elevation (Z coordinate) of a GPX way point.
	 * 
	 * @param wpt
	 *            The way point instance.
	 * @return The x coordinate or 0, if the given way point is null or contains
	 *         not height attribute.
	 */
	private static double getInternalElevation(WayPoint wpt) {
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
	    	/*
		int lat = (int)Math.round(wpt.getCoor().lat());
		int lon = (int)Math.round(wpt.getCoor().lon());
		byte geoid = GeoidData.getGeoid(lat, lon);
		
		System.out.println(
				String.format("Geoid(%d, %d) = %d", lat, lon, geoid));
	    	 */
		return 0;
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
