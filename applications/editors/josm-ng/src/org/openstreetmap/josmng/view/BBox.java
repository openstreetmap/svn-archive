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

import java.awt.Point;
import java.awt.Rectangle;

/**
 * An object similar to java.awt.Rectangle, but careful about data-type
 * overflows and better suited for collecting bounds of a bunch of points.
 * 
 * @author nenik
 */
public class BBox {
    private int x1, y1;
    private int x2, y2;
    
    /** create an empty bbox
     * 
     */
    public BBox () {
       x1 = y1 = Integer.MAX_VALUE;
       x2 = y2 = Integer.MIN_VALUE;
    }
    
    public BBox (int x1, int y1, int x2, int y2) {
        this();
        addPoint(x1, y1);
        addPoint(x2, y2);
    }
    
    public int getX() {
        return x1;
    }
    
    public int getY() {
        return y1;
    }
    
    public long getWidth() {
        return (long)x2-x1;
    }
    
    public long getHeight() {
        return (long)y2-y1;
    }
    
    public void addPoint(Point p) {
        addPoint(p.x, p.y);
    }

    public void addPoint(ViewCoords vc) {
        addPoint(vc.getIntLon(), vc.getIntLat());
    }
    
    public void addPoint(int x, int y) {
        x1 = Math.min(x1, x);
        x2 = Math.max(x2, x);
        y1 = Math.min(y1, y);
        y2 = Math.max(y2, y);
    }
  
    public void add(BBox bbox) {
        x1 = Math.min(x1, bbox.x1);
        x2 = Math.max(x2, bbox.x2);
        y1 = Math.min(y1, bbox.y1);
        y2 = Math.max(y2, bbox.y2);
    }
    
    public boolean contains(int x, int y) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }
    
    public boolean intersects(BBox other) {
        return x2 > other.x1 && x1 < other.x2 && y2 > other.y1 && y1 < other.y2;
    }

    public boolean contains(BBox other) {
        return other.x1 >= x1 && other.x2 <= x2 && other.y1 >= y1 && other.y2 <= y2;
    }

    public ViewCoords getTopLeft() {
        return new ViewCoords.Impl(x1, y1);
    }

    public ViewCoords getBottomRight() {
        return new ViewCoords.Impl(x2, y2);
    }
    
    public ViewCoords getCenter() {
        int x = (int)(((long)x1 + x2)/2);
        int y = (int)(((long)y1 + y2)/2);
        return new ViewCoords.Impl(x, y);
    }
    
    public Rectangle toRectangle() {
        return new Rectangle(x1, y1, (int)getWidth(), (int)getHeight());

    }
    
    /**
     * Computes the position of this bbox against given point. The point divides
     * the space to 4 quadrands, south-west(1), north-west(2), south-east(4)
     * and north-east(8).
     *   
     * @param x x of the point
     * @param y y of the point
     * @return the bitvector with 1 set for each quadrant that intercects
     * with this bbox.
     */
            // 1:sw, 2:nw, 4:se, 8:ne
    public int getCoincidenceVector(int x, int y) {
        int vec = 0;

        if (x1 < x) {
            if (y1 < y) vec = 1;
            if (y2 >= y) vec |= 2;
        }
        if (x2 >= x) {
            if (y1 < y) vec |= 4;
            if (y2 >= y) vec |= 8;
        }
        assert vec > 0;
        return vec;
    }
}
