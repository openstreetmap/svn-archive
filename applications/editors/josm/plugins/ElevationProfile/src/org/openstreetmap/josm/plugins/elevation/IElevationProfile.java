// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation;

import java.util.Date;
import java.util.List;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 * Interface for an elevation profile providing special properties/values.
 */
public interface IElevationProfile {
    /**
     * Gets the name of the elevation profile.
     */
    String getName();

    /**
     * Gets the time stamp of first recorded track point.
     */
    Date getStart();

    /**
     * Gets the time stamp of last recorded track point.
     */
    Date getEnd();

    /**
     * Gets the minimum elevation height of all tracks and routes.
     */
    int getMinHeight();

    /**
     * Gets the maximum elevation height of all tracks and routes.
     */
    int getMaxHeight();

    /**
     * Gets the distance of the track in kilometers.
     */
    double getDistance();

    /**
     * Gets the average elevation height of all tracks and routes.
     */
    int getAverageHeight();

    /**
     * Gets the difference between min and max elevation.
     */
    int getHeightDifference();

    /**
     * Gets the elevation gain.
     *
     */
    int getGain();

    /**
     * Gets the total number of way points (sum of all way points of all tracks and routes).
     */
    int getNumberOfWayPoints();

    /**
     * Gets the list containing the way points.
     */
    List<WayPoint> getWayPoints();

    /**
     * Gets the first recorded way point.
     */
    WayPoint getStartWayPoint();

    /**
     * Gets the last recorded way point.
     */
    WayPoint getEndWayPoint();

    /**
     * Gets the way point with the highest elevation value.
     */
    WayPoint getMaxWayPoint();

    /**
     * Gets the way point with the lowest elevation value.
     */
    WayPoint getMinWayPoint();

    /**
     * Gets a flag indicating whether the associated way points
     * contained elevation data or not. This is the case if min
     * and max height are equal.
     */
    boolean hasElevationData();

    /**
     * Returns the time between start and end of the track.
     */
    long getTimeDifference();

    /**
     * Gets the elevation value for at the given data index point.
     */
    int elevationValueAt(int i);

    /**
     * Gets the coordinate bounds of the elevation profile.
     *
     * @return the bounds
     */
    Bounds getBounds();

    /**
     * Gets the children of the segment (maybe null).
     */
    List<IElevationProfile> getChildren();

    /**
     * Gets the parent of the elevation profile.
     */
    IElevationProfile getParent();

    /**
     * Triggers model refresh.
     */
    void updateElevationData();
}
