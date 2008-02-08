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

package org.openstreetmap.josmng.view.projection;

import java.awt.geom.Point2D;

import org.openstreetmap.josmng.view.*;

/**
 * A Mercator projection implementation.
 * 
 * @author nenik
 */
public final class Mercator implements Projection.Impl {
    
    public Point2D lonLatToPoint(double lon, double lat) {
        return new Point2D.Double(lon*Math.PI/180,
                Math.log(Math.tan(Math.PI/4 + lat*Math.PI/360)));
    }

    public Point2D pointToLonLat(double east, double north) {
        return new Point2D.Double(east*180/Math.PI,
                Math.atan(Math.sinh(north))*180/Math.PI);
    }

    public String getName() {
        return "Mercator";
    }
}
