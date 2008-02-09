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
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nenik
 */
public class MercatorTest {

    Mercator instance = new Mercator();
    Point2D zero = new Point2D.Double();
    
    public MercatorTest() {}

    public @Test void lonLatToPointZero() {
        comparePoints("Zero stays zero", zero, instance.lonLatToPoint(0d, 0d));
    }

    public @Test void pointToLonLatZero() {
        comparePoints("Zero stays zero", zero, instance.pointToLonLat(0d, 0d));
    }
    

    public @Test void convertBackRandomPoint() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        String failure = "Double conversion for seed=" + seed;
        
        for (int i=0; i<100; i++) {
            Point2D p = new Point2D.Double(360*(random.nextDouble()-0.5d),
                    180*(random.nextDouble()-0.5d));
            Point2D tmp = instance.lonLatToPoint(p.getX(), p.getY());
            Point2D val = instance.pointToLonLat(tmp.getX(), tmp.getY());
            comparePoints(failure, p, val);
        }
    }


    private static final double EPSILON = 1e-12;
    
    private void comparePoints(String message, Point2D expected, Point2D val) {
        assertEquals(message, expected.getX(), val.getX(), EPSILON);
        assertEquals(message, expected.getY(), val.getY(), EPSILON);
    }
}