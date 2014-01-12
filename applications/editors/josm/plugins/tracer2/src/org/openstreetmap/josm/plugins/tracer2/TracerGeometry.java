/**
 *  Tracer2 - plug-in for JOSM to capture contours
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2;

import org.openstreetmap.josm.data.coor.LatLon;

public class TracerGeometry {

    /**
     * Angle between line AB and CD
     * @param a Point A.
     * @param b Point B.
     * @param c Point C.
     * @param d Point D.
     * @return Angle in degrees.
     */
    static public double angleOfLines(LatLon a, LatLon b, LatLon c, LatLon d) {
        return (Math.abs(
                    Math.atan2(a.lat() - b.lat(), a.lon() - b.lon()) -
                    Math.atan2(c.lat() - d.lat(), c.lon() - d.lon())
                ) / Math.PI * 180) % 360;
    }

    /**
     * Distance of point C from line segment AB.
     * @param c Point C.
     * @param a Point A.
     * @param b Point B.
     * @return Distance.
     */
    static public double distanceFromSegment(LatLon c, LatLon a, LatLon b) {
        return distanceFromSegment(
                c.getX(), c.getY(),
                a.getX(), a.getY(),
                b.getX(), b.getY()
        );
    }

    static private double distanceFromSegment(double cx, double cy, double ax, double ay, double bx, double by) {
        double r_numerator = (cx - ax) * (bx - ax) + (cy - ay) * (by - ay);
        double r_denomenator = (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
        if(r_denomenator == 0)System.out.println("r_denomenator == 0    ------------");
        double r = r_numerator / r_denomenator;
        double s = ((ay - cy) * (bx - ax) - (ax - cx) * (by - ay)) / r_denomenator;
        double distanceLine = Math.abs(s) * Math.sqrt(r_denomenator);

        if ((r >= 0) && (r <= 1)) {
            return distanceLine;
        } else {
            double dist1 = (cx - ax) * (cx - ax) + (cy - ay) * (cy - ay);
            double dist2 = (cx - bx) * (cx - bx) + (cy - by) * (cy - by);
            if (dist1 < dist2) {
                return Math.sqrt(dist1);
            } else {
                return Math.sqrt(dist2);
            }
        }
    }
}
