/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
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

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.osm;

/**
 * Representation of an earth surface area that could be represented
 * as a restangle in the EPSG4326 projection. That is, a simple span longitude
 * by a single span of latitude.
 * 
 * This class is immutable, if there is some math to be performed on Bounds,
 * a new instance has to be created.
 * 
 * The coordinates are always in normalized form, that is, the {@link #min}
 * coordinate contains both minimal longitude and minimal latitude, while
 * the {@link max} coordinate have both latitude and longitude maximums.
 * 
 * @author nenik
 */
public final class Bounds {
    
    public final Coordinate min;
    public final Coordinate max;

    public static final Bounds WORLD = new Bounds(
            -85.05112877980659, -180, 85.05112877980659, 180);
    /**
     * This constructor must be private to guard the immutability and normality
     * invariants.
     * 
     * @param min the most southwest point
     * @param max the most northeast point
     */
    private Bounds(Coordinate min, Coordinate max) {
        this.min = min;
        this.max = max;
    }
    
    private Bounds(double minLat, double minLon, double maxLat, double maxLon) {
        this(new CoordinateImpl(minLat, minLon), new CoordinateImpl(maxLat, maxLon));
    }
    
    public static Bounds create(Coordinate from, Coordinate to) {
        // check whether is it safe to use passed instances
        if (from instanceof CoordinateImpl && to instanceof CoordinateImpl &&
                from.getLatitude() <= to.getLatitude() &&
                from.getLongitude() <= to.getLongitude()) {
            return new Bounds(from, to);
        } else {
            return new Bounds(
                    Math.min(from.getLatitude(), to.getLatitude()),
                    Math.min(from.getLongitude(), to.getLongitude()),
                    Math.max(from.getLatitude(), to.getLatitude()),
                    Math.max(from.getLongitude(), to.getLongitude()));
        }
    }
    
    /**
     * Is given point within the bounds? Points on the bounds boundary
     * are deemed "within" the bounds.
     * @param coor the point to test.
     * @return true iff the point is within this Bounds.
     */
    public boolean contains(Coordinate coor) {
        return min.getLatitude() <= coor.getLatitude() &&
                coor.getLatitude() <= max.getLatitude() &&
                min.getLongitude() <= coor.getLongitude() &&
                coor.getLongitude() <= max.getLongitude();
    }

    public Bounds union(Bounds b2) {
        return new Bounds(
                Math.min(min.getLatitude(), b2.min.getLatitude()),
                Math.min(min.getLongitude(), b2.min.getLongitude()),
                Math.max(max.getLatitude(), b2.max.getLatitude()),
                Math.max(max.getLongitude(), b2.max.getLongitude()));
    }
    
    public @Override String toString() {
        return "[" + min.getLatitude() + "," + min.getLongitude() + "; " +
                max.getLatitude() + "," + max.getLongitude() + "]";
    }
}
