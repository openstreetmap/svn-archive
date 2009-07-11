package org.openstreetmap.josm.plugins.routes.paint;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public abstract class AbstractLinePainter implements PathPainter {
	
	// Following two method copied from http://blog.persistent.info/2004/03/java-lineline-intersections.html
	protected boolean getLineLineIntersection(Line2D.Double l1,
			Line2D.Double l2,
			Point2D.Double intersection)
	{
		double  x1 = l1.getX1(), y1 = l1.getY1(),
		x2 = l1.getX2(), y2 = l1.getY2(),
		x3 = l2.getX1(), y3 = l2.getY1(),
		x4 = l2.getX2(), y4 = l2.getY2();
		
		double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3)*(y2 - y1));
		intersection.x = x1 + ua * (x2 - x1);
		intersection.y = y1 + ua * (y2 - y1);
		 

/*		intersection.x = det(det(x1, y1, x2, y2), x1 - x2,
				det(x3, y3, x4, y4), x3 - x4)/
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		intersection.y = det(det(x1, y1, x2, y2), y1 - y2,
				det(x3, y3, x4, y4), y3 - y4)/
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);*/

		return true;
	}

	protected double det(double a, double b, double c, double d)
	{
		return a * d - b * c;
	}

	protected Point2D shiftPoint(Point2D p1, Point2D p2, double shift) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		
		// Perpendicular vector
		double ndx = -dy;
		double ndy = dx;
		
		// Normalize
		double length = Math.sqrt(ndx * ndx + ndy * ndy);
		ndx = ndx / length;
		ndy = ndy / length;
		
		return new Point2D.Double(p1.getX() + shift * ndx, p1.getY() + shift * ndy);
	}
	
	protected Line2D.Double shiftLine(Point2D p1, Point2D p2, double shift) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();

		Point2D point1 = shiftPoint(p1, p2, shift);
		Point2D point2 = new Point2D.Double(point1.getX() + dx, point1.getY() + dy);
	
		return new Line2D.Double(
				point1, point2);
	}

}
