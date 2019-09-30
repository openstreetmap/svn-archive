// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.SystemOfMeasurement;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.elevation.gpx.GeoidCorrectionKind;
import org.openstreetmap.josm.tools.Logging;

/**
 * Provides methods to access way point attributes and some utility methods regarding elevation stuff (
 * e. g. special text formats, unit conversion, geoid calc).
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public final class ElevationHelper {

    private ElevationHelper() {
        // Hide default constructor for utilities classes
    }

    /** The 'no elevation' data magic. */
    public static final double NO_ELEVATION = Double.NaN;

    /**
     * The name of the elevation height of a way point.
     */
    public static final String HEIGHT_ATTRIBUTE = "ele";

    private static GeoidCorrectionKind geoidKind = GeoidCorrectionKind.None;

    /** The HGT reader instance. */
    private static final HgtReader hgt = new HgtReader();

    /**
     * Gets the current mode of GEOID correction.
     */
    public static GeoidCorrectionKind getGeoidKind() {
        return geoidKind;
    }

    public static void setGeoidKind(GeoidCorrectionKind geoidKind) {
        ElevationHelper.geoidKind = geoidKind;
    }

    /**
     * Checks if given value is a valid elevation value.
     *
     * @param ele the ele
     * @return true, if is valid elevation
     */
    public static boolean isValidElevation(double ele) {
        return !Double.isNaN(ele);
    }

    /**
     * Gets the elevation (Z coordinate) of a GPX way point in meter or feet (for
     * US, UK, ZA, AU, NZ and CA).
     *
     * @param wpt
     *            The way point instance.
     * @return The x coordinate or <code>NO_ELEVATION</code>, if the given way point is null or contains
     *         not height attribute.
     */
    public static double getElevation(WayPoint wpt) {
        if (wpt == null) return NO_ELEVATION;

        // try to get elevation from HGT file
        double eleInt = getSrtmElevation(wpt.getCoor());
        if (isValidElevation(eleInt)) {
            return eleInt;
        }

        // no HGT, check for elevation data in GPX
        if (!wpt.attr.containsKey(HEIGHT_ATTRIBUTE)) {
            // GPX has no elevation data :-(
            return NO_ELEVATION;
        }

        // Parse elevation from GPX data
        String height = wpt.getString(ElevationHelper.HEIGHT_ATTRIBUTE);
        try {
            return Double.parseDouble(height);
        } catch (NumberFormatException e) {
            Logging.error(String.format("Cannot parse double from '%s': %s", height, e.getMessage()));
            return NO_ELEVATION;
        }
    }

    private static double getElevation(LatLon ll) {
        return getSrtmElevation(ll);
    }

    /**
     * Computes the slope <b>in percent</b> between two way points. E. g. an elevation gain of 12m
     * within a distance of 100m is equal to a slope of 12%.
     *
     * @param w1 the first way point
     * @param w2 the second way point
     * @return the slope in percent
     */
    public static double computeSlope(LatLon w1, LatLon w2) {
        // same coordinates? -> return 0, if yes
        if (w1.equals(w2)) return 0;

        // get distance in meters and divide it by 100 in advance
        double distInMeter = w1.greatCircleDistance(w2) / 100.0;

        // get elevation (difference)
        int ele1 = (int) ElevationHelper.getElevation(w1);
        int ele2 = (int) ElevationHelper.getElevation(w2);
        int dH = ele2 - ele1;

        // Slope in percent is define as elevation gain/loss in meters related to a distance of 100m
        return dH / distInMeter;
    }

    /**
     * Gets the elevation string for a given elevation, e. g "300m" or "800ft".
     */
    public static String getElevationText(int elevation) {
        return SystemOfMeasurement.getSystemOfMeasurement().getDistText(elevation);
    }

    /**
     * Gets the elevation string for a given elevation, e. g "300m" or "800ft".
     */
    public static String getElevationText(double elevation) {
        return SystemOfMeasurement.getSystemOfMeasurement().getDistText((int) Math.round(elevation));
    }

    /**
     * Gets the elevation string for a given way point, e. g "300m" or "800ft".
     *
     * @param wpt the way point
     * @return the elevation text
     */
    public static String getElevationText(WayPoint wpt) {
        if (wpt == null) return "-";

        return getElevationText(ElevationHelper.getElevation(wpt));
    }

    /**
     * Get the time string for a given way point.
     */
    public static String getTimeText(WayPoint wpt) {
        if (wpt == null) return null;

        int hour = ElevationHelper.getHourOfWayPoint(wpt);
        int min = ElevationHelper.getMinuteOfWayPoint(wpt);
        return String.format("%02d:%02d", hour, min);
    }

    /**
     * Gets the SRTM elevation (Z coordinate) of the given coordinate.
     *
     * @param ll
     *            The coordinate.
     * @return The z coordinate or {@link Double#NaN}, if elevation value could not be obtained
     *         not height attribute.
     */
    public static double getSrtmElevation(LatLon ll) {
        if (ll != null) {
            // Try to read data from SRTM file
            // TODO: Option to switch this off
            double eleHgt = hgt.getElevationFromHgt(ll);

            if (isValidElevation(eleHgt)) {
                return eleHgt;
            }
        }
        return NO_ELEVATION;
    }

    /**
     * Checks given area for SRTM data.
     *
     * @param bounds the bounds/area to check
     * @return true, if SRTM data are present; otherwise false
     */
    public static boolean hasSrtmData(Bounds bounds) {
        if (bounds == null) return false;

        LatLon tl = bounds.getMin();
        LatLon br = bounds.getMax();

        return isValidElevation(getSrtmElevation(tl)) &&
                isValidElevation(getSrtmElevation(br));
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

        List<WayPoint> res = new ArrayList<>(targetSize);
        for (int i = 0; i < origSize; i += delta) {
            res.add(origList.get(i));
        }

        return res;
    }

    /**
     * Gets the hour value of a way point in 24h format.
     */
    public static int getHourOfWayPoint(WayPoint wpt) {
        if (wpt == null) return -1;

        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(wpt.getDate());   // assigns calendar to given date
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Gets the minute value of a way point in 24h format.
     */
    public static int getMinuteOfWayPoint(WayPoint wpt) {
        if (wpt == null) return -1;

        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(wpt.getDate());   // assigns calendar to given date
        return calendar.get(Calendar.MINUTE);
    }
}
