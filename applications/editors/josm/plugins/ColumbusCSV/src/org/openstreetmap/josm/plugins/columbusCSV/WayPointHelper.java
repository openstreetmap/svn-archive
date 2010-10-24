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

package org.openstreetmap.josm.plugins.columbusCSV;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * @author Oliver Wieland <oliver.wieland@online.de> Provides several static methods to access way point
 *         attributes.
 */
public class WayPointHelper {
	/**
	 * The name of the elevation height of a way point.
	 */
	public static final String HEIGHT_ATTRIBUTE = "ele";


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
	
	public static double getLonDist(WayPoint w1, WayPoint w2) {
		LatLon ll = new LatLon(w1.getCoor().lat(), w2.getCoor().lon());
		return w1.getCoor().greatCircleDistance(ll);
	}
	
	public static double getLatDist(WayPoint w1, WayPoint w2) {
		LatLon ll = new LatLon(w2.getCoor().lat(), w1.getCoor().lon());
		return w1.getCoor().greatCircleDistance(ll);
	}
	
	/**
	 * Moves a given lat/lon coordinate by a given amount of meters in
	 * x and y direction.
	 * @param src The original lat/lon coordinate.
	 * @param dlat The distance in latitude direction in meters
	 * @param dlon The distance in longitude direction in meters
	 * @return 
	 */
	public static LatLon moveLatLon(LatLon src, double dlat, double dlon) {
		final double R = 6378135;
		
		double lat1 = toRadians(src.lat());
		double lon1 = toRadians(src.lon());
		
		double dlonsin2 = sin(dlon/2 / R);
		double dlatsin2 = sin(dlat/2 / R);
		double dlatcos = cos(lon1);
		
		double lon2rad = 2 * asin(sqrt(dlonsin2 * dlonsin2 / dlatcos/dlatcos)) + lon1;
		double lat2rad = 2 * asin(dlatsin2) + lat1;
		
		double lon2 = toDegrees(lon2rad);
		double lat2 = toDegrees(lat2rad);
		
		LatLon llmoved = new LatLon(lat2, lon2);
		
		//double d2 = llmoved.greatCircleDistance(src);
		
		return llmoved;
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
}
