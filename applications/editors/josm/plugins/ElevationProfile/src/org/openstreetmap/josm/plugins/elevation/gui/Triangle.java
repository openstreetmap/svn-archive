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
 * @author Oliver
 * 
 */
/*
 * Class to represent a Triangle. Implementing the java.awt.Shape inteface.
 */
public class Triangle implements Shape {
	private Polygon poly;

	public Triangle(Polygon p) {
		poly = p;
	}

	public Triangle(Point p1, Point p2, Point p3) {
		poly = new Polygon();
		poly.addPoint(p1.x, p1.y);
		poly.addPoint(p2.x, p2.y);
		poly.addPoint(p3.x, p3.y);		
	}

	public void draw(Graphics g) {
		g.drawPolygon(poly);
	}

	public void fill(Graphics g) {
		g.fillPolygon(poly);
	}

	// methods implemented from interface Shape

	public Rectangle getBounds() {
		return poly.getBounds();
	}

	public Rectangle2D getBounds2D() {
		return poly.getBounds2D();
	}

	public boolean contains(double x, double y) {
		return poly.contains(x, y);
	}

	public boolean contains(Point2D p) {
		return poly.contains(p);
	}

	public boolean intersects(double x, double y, double w, double h) {
		return poly.intersects(x, y, w, h);
	}

	public boolean intersects(Rectangle2D r) {
		return poly.intersects(r);
	}

	public boolean contains(double x, double y, double w, double h) {
		return poly.contains(x, y, w, h);
	}

	public boolean contains(Rectangle2D r) {
		return poly.intersects(r);
	}

	public PathIterator getPathIterator(AffineTransform at) {
		return poly.getPathIterator(at);
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return poly.getPathIterator(at, flatness);
	}

}