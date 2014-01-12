/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

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
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Class to represent a triangle shape. {@see java.awt.Shape}.
 */
public class Triangle implements Shape {
	private Polygon poly;

	/**
	 * Copy constructor.
	 * @param p
	 */
	public Triangle(Polygon p) {
		if (p == null || p.npoints != 3) throw new RuntimeException("Given polygon was null or had invalid number of points");
		poly = p;
	}

	/**
	 * Creates a triangle from 3 given points. The points are used without any sanity check, so it is up to
	 * the user that the points form a triangle.
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public Triangle(Point p1, Point p2, Point p3) {
		poly = new Polygon();
		poly.addPoint(p1.x, p1.y);
		poly.addPoint(p2.x, p2.y);
		poly.addPoint(p3.x, p3.y);		
	}

	/**
	 * Draws an outlined triangle.
	 * @param g
	 */
	public void draw(Graphics g) {
		g.drawPolygon(poly);
	}

	/**
	 * Draws a filled triangle.
	 * @param g
	 */
	public void fill(Graphics g) {
		g.fillPolygon(poly);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#getBounds()
	 */
	public Rectangle getBounds() {
		return poly.getBounds();
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#getBounds2D()
	 */
	public Rectangle2D getBounds2D() {
		return poly.getBounds2D();
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#contains(double, double)
	 */
	public boolean contains(double x, double y) {
		return poly.contains(x, y);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#contains(java.awt.geom.Point2D)
	 */
	public boolean contains(Point2D p) {
		return poly.contains(p);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#intersects(double, double, double, double)
	 */
	public boolean intersects(double x, double y, double w, double h) {
		return poly.intersects(x, y, w, h);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D r) {
		return poly.intersects(r);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#contains(double, double, double, double)
	 */
	public boolean contains(double x, double y, double w, double h) {
		return poly.contains(x, y, w, h);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#contains(java.awt.geom.Rectangle2D)
	 */
	public boolean contains(Rectangle2D r) {
		return poly.intersects(r);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform)
	 */
	public PathIterator getPathIterator(AffineTransform at) {
		return poly.getPathIterator(at);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform, double)
	 */
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return poly.getPathIterator(at, flatness);
	}

}