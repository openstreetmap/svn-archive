// License: GPL. For details, see LICENSE file.
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
import org.openstreetmap.josm.tools.Logging;

/**
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt; Provides several static methods to access way point
 *         attributes.
 */
public class WayPointHelper {
    /**
     * The name of the elevation height of a way point.
     */
    public static final String HEIGHT_ATTRIBUTE = "ele";

    private static final double R = 6378135;
    
    private WayPointHelper() {
       // Private constructor for the utility class. 
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
                return Double.parseDouble(height);
            } catch (NumberFormatException e) {
                Logging.error(String.format(
                        "Cannot parse double from '%s': %s", height, e
                                .getMessage()));
            }
        }
        return 0;
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
     * @return new lat/lon coordinates
     */
    public static LatLon moveLatLon(LatLon src, double dlat, double dlon) {
        double lat1 = toRadians(src.lat());
        double lon1 = toRadians(src.lon());
        
        double dlonsin2 = sin(dlon/2 / R);
        double dlatsin2 = sin(dlat/2 / R);
        double dlatcos = cos(lon1);
        
        double lon2rad = 2 * asin(sqrt(dlonsin2 * dlonsin2 / dlatcos/dlatcos)) + lon1;
        double lat2rad = 2 * asin(dlatsin2) + lat1;
        
        double lon2 = toDegrees(lon2rad);
        double lat2 = toDegrees(lat2rad);
        
        return new LatLon(lat2, lon2);
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

        List<WayPoint> res = new ArrayList<>(targetSize);
        for (int i = 0; i < origSize; i += delta) {
            res.add(origList.get(i));
        }

        return res;
    }
}
