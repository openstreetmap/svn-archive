package org.openstreetmap.gui.jmapviewer.interfaces;

//License: GPL. Copyright 2009 by Stefan Zeller

import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * Interface to be implemented by squares that can be displayed on the map.
 *
 * @author Stefan Zeller
 * @see JMapViewer#addMapSquare(MapSquare)
 * @see JMapViewer#getMapSquareList()
 * @date 21.06.2009S
 */
public interface MapSquare {

    /**
     * @return Latitude/Longitude of top left of square
     */
    public Coordinate getTopLeft();

    /**
     * @return Latitude/Longitude of bottom right of square
     */
    public Coordinate getBottomRight();

    /**
     * Paints the map square on the map. The <code>topLeft</code> and
     * <code>bottomRight</code> specifies the coordinates within <code>g</code>
     *
     * @param g
     * @param position
     */
    public void paint(Graphics g, Point topLeft, Point bottomRight);
}
