/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import java.awt.Color;
import java.awt.Point;

import org.openstreetmap.josm.gui.MapView;

/**
 * @author tilusnet <tilusnet@gmail.com> The basic segment treated as reference.
 * 
 */
public class AlignWaysRefSegment extends AlignWaysSegment {

    // Note: segment may be null. This is normal.

    public AlignWaysRefSegment(MapView mapview, Point p)
    throws IllegalArgumentException {
        super(mapview, p);
        setSegment(getNearestWaySegment(p));
        segmentColor = Color.GREEN;

    }

}
