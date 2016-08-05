// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gui;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 * Class to represent a triangle shape. {@see java.awt.Shape}.
 */
public class Triangle implements Shape {
    private final Polygon poly;

    /**
     * Copy constructor.
     */
    public Triangle(Polygon p) {
        if (p == null || p.npoints != 3) throw new RuntimeException("Given polygon was null or had invalid number of points");
        poly = p;
    }

    /**
     * Creates a triangle from 3 given points. The points are used without any sanity check, so it is up to
     * the user that the points form a triangle.
     */
    public Triangle(Point p1, Point p2, Point p3) {
        poly = new Polygon();
        poly.addPoint(p1.x, p1.y);
        poly.addPoint(p2.x, p2.y);
        poly.addPoint(p3.x, p3.y);
    }

    /**
     * Draws an outlined triangle.
     */
    public void draw(Graphics g) {
        g.drawPolygon(poly);
    }

    /**
     * Draws a filled triangle.
     */
    public void fill(Graphics g) {
        g.fillPolygon(poly);
    }

    @Override
    public Rectangle getBounds() {
        return poly.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        return poly.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y) {
        return poly.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return poly.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return poly.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return poly.intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return poly.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return poly.intersects(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return poly.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return poly.getPathIterator(at, flatness);
    }
}
