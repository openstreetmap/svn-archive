/*
 * This file is part of ImproveWayAccuracy plugin for JOSM.
 * http://wiki.openstreetmap.org/wiki/JOSM/Plugins/ImproveWayAccuracy
 * 
 * Licence: GPL v2 or later
 * Author:  Alexander Kachkaev <alexander@kachkaev.ru>, 2011
 */
package org.openstreetmap.josm.plugins.iwa;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.tools.Geometry;

/**
 * This static class contains geometry functions used by the plugin.
 */
public class IWAGeometry {

    /**
     * Returns angle of a segment defined with 2 point coordinates.
     * 
     * @param p1
     * @param p2
     * @return Angle in radians (-pi, pi]
     */
    public static double getSegmentAngle(EastNorth p1, EastNorth p2) {
        return Math.atan2(p2.north() - p1.north(), p2.east() - p1.east());
    }

    /**
     * Returns angle of a corner defined with 3 point coordinates.
     * 
     * @param p1
     * @param p2
     *            Common endpoint
     * @param p3
     * @return Angle in radians (-pi, pi]
     */
    public static double getCornerAngle(EastNorth p1, EastNorth p2, EastNorth p3) {
        Double result = getSegmentAngle(p2, p1) - getSegmentAngle(p2, p3);
        if (result <= -Math.PI)
            result += 2 * Math.PI;

        if (result > Math.PI)
            result -= 2 * Math.PI;

        return result;
    }

    /**
     * Returns the coordinate of intersection of segment sp1-sp2 and an altitude
     * to it starting at point ap. If the line defined with sp1-sp2 intersects
     * its altitude out of sp1-sp2, null is returned.
     * 
     * @param sp1
     * @param sp2
     * @param ap
     * @return Intersection coordinate or null
     */
    public static EastNorth getSegmentAltituteIntersection(EastNorth sp1,
            EastNorth sp2, EastNorth ap) {
        Double segmentLenght = sp1.distance(sp2);
        Double altitudeAngle = getSegmentAngle(sp1, sp2) + Math.PI / 2;

        // Taking a random point on the altitude line (angle is known).
        EastNorth ap2 = new EastNorth(ap.east() + 1000
                * Math.cos(altitudeAngle), ap.north() + 1000
                * Math.sin(altitudeAngle));

        // Finding the intersection of two lines
        EastNorth resultCandidate = Geometry.getLineLineIntersection(sp1, sp2,
                ap, ap2);

        // Filtering result
        if (resultCandidate != null
                && resultCandidate.distance(sp1) * .999 < segmentLenght
                && resultCandidate.distance(sp2) * .999 < segmentLenght)
            return resultCandidate;
        else
            return null;
    }

}
