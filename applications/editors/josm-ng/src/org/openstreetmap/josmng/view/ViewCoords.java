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

package org.openstreetmap.josmng.view;

/**
 * Coordinates in the view space. They represent "northings and "eastings",
 * that is, latitude and longitude after view transformation, moreover
 * normalized into a space of <-1,1> x <-1,1> (or subset thereof for projections
 * with different aspect ratio) and encoded as (binary) shift-dot integers for
 * easy view scaling.
 * 
 * The precision of the coordinates is (depending on the actually used
 * projection) about 1/12.000.000th of a degree (a unit),
 * that is, degrees to over 7 valid digits, or 10mm on equator per unit.
 * 
 * The coordinates gets converted to screen coordinates by dividing with
 * current scale, where scale is the number of units per pixel.
 * 
 * As the scale is encoded in such inverse values, large zooms can be coarse.
 * But even for a 20% zoom step, when the largest availabe zoom in 6,
 * a pixel would represent ~6cm on equator which is very comfortable
 * detail level for OSM purposes.
 * 
 * @author nenik
 */
public class ViewCoords {
    private int x;
    private int y;
    
    ViewCoords() {
    }   
    
    public ViewCoords(int lon, int lat) {
        this.x = lon;
        this.y = lat;
    }

    protected void setCoordinates(ViewCoords from) {
        x = from.x;
        y = from.y;
    }
    
    public int getIntLon() {
        return x;
    }

    public int getIntLat() {
        return y;
    }

    /**
     * Computes a ViewCoords shifted by the difference between from
     * and to.
     * @param from First reference point
     * @param to Second reference point
     * @return a ViewCoords that is shifted from this ViewCoords the same way
     * as point <code>to</code> is from point <code>from</code>.
     */
    public final ViewCoords movedByDelta(ViewCoords from, ViewCoords to) {
        return new ViewCoords(getIntLon() + from.getIntLon() - to.getIntLon(),
                    getIntLat() + from.getIntLat() - to.getIntLat());
    }
}
