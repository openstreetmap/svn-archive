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

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.CoordinateImpl;

/**
 *
 * @author nenik
 */
public class ProjectionTest {

    Projection instance = Projection.getAvailableProjections().iterator().next();
    Coordinate zero = new CoordinateImpl(0, 0);
    ViewCoords vc_zero = new ViewCoords(0, 0);

    public ProjectionTest() {}

    public @Test void lonLatToPointZero() {
        compareCoordinates("Zero stays zero", zero, instance.viewToCoord(vc_zero));
    }

    public @Test void pointToLonLatZero() {
        compareVC("Zero stays zero", vc_zero, instance.coordToView(zero));
    }
    
    public @Test void convertBack() {
        Coordinate coor = new CoordinateImpl(85d,90d);
        ViewCoords tmp = instance.coordToView(coor);
        Coordinate val = instance.viewToCoord(tmp);
        compareCoordinates("Miss", coor, val);
    }

    public @Test void convertBackRandomPoint() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        String failure = "Double conversion for seed=" + seed;
        
        for (int i=0; i<100; i++) {
            Coordinate coor = new CoordinateImpl(170*(random.nextDouble()-0.5d),
                    360*(random.nextDouble()-0.5d));
            ViewCoords tmp = instance.coordToView(coor);
            Coordinate val = instance.viewToCoord(tmp);
            compareCoordinates(failure, coor, val);
        }
    }


    private static final double EPSILON = 1e-6;
    
    private void compareCoordinates(String message, Coordinate expected, Coordinate val) {
        assertEquals(message, expected.getLongitude(), val.getLongitude(), EPSILON);
        assertEquals(message, expected.getLatitude(), val.getLatitude(), EPSILON);
    }

    private void compareVC(String message, ViewCoords expected, ViewCoords val) {
        assertEquals(message, expected.getIntLon(), val.getIntLon());
        assertEquals(message, expected.getIntLat(), val.getIntLat());
    }
}