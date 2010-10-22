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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.elevation.ElevationWayPointKind;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.WayPointHelper;

/**
 * @author Oliver
 * 
 */
public class DefaultElevationProfileRenderer implements
		IElevationProfileRenderer {

	private static final int ROUND_RECT_RADIUS = 8;
	/**
	 * 
	 */
	private static final int TRIANGLE_BASESIZE = 24;
	/**
	 * 
	 */
	private static final int BASIC_WPT_RADIUS = 2;
	private static final int REGULAR_WPT_RADIUS = BASIC_WPT_RADIUS * 4;
	private static final int BIG_WPT_RADIUS = BASIC_WPT_RADIUS * 10;
	
	// predefined colors
	private static final Color HIGH_COLOR = ElevationColors.EPMidBlue;
	private static final Color LOW_COLOR = ElevationColors.EPMidBlue;
	private static final Color START_COLOR = Color.GREEN;
	private static final Color END_POINT = Color.RED;
	private static final Color MARKER_POINT = Color.YELLOW;
	// Predefined radians
	private static final double RAD_180 = Math.PI;
	// private static final double RAD_270 = Math.PI * 1.5;
	private static final double RAD_90 = Math.PI * 0.5;
	
	private List<Rectangle> forbiddenRects = new ArrayList<Rectangle>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openstreetmap.josm.plugins.elevation.gui.IElevationProfileRenderer
	 * #getColorForWaypoint
	 * (org.openstreetmap.josm.plugins.elevation.IElevationProfile,
	 * org.openstreetmap.josm.data.gpx.WayPoint,
	 * org.openstreetmap.josm.plugins.elevation.ElevationWayPointKind)
	 */
	public Color getColorForWaypoint(IElevationProfile profile, WayPoint wpt,
			ElevationWayPointKind kind) {

		if (wpt == null || profile == null) {
			System.err.println(String.format(
					"Cannot determine color: prof=%s, wpt=%s", profile, wpt));
			return null;
		}

		int z = (int) WayPointHelper.getElevation(wpt);

		switch (kind) {
		case Plain:
		case ElevationLevel:
			if (z > profile.getAverageHeight()) {
				return HIGH_COLOR;
			} else {
				return LOW_COLOR;
			}
		case Highlighted:
			return Color.ORANGE;
		case ElevationGain:
			return Color.GREEN;
		case ElevationLoss:
			return Color.RED;
		case FullHour:
			return MARKER_POINT;
		case MaxElevation:
			return HIGH_COLOR;
		case MinElevation:
			return LOW_COLOR;
		
		case StartPoint:
			return START_COLOR;
		case EndPoint:
			return END_POINT;
		}

		throw new RuntimeException("Unknown way point kind: " + kind);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openstreetmap.josm.plugins.elevation.gui.IElevationProfileRenderer
	 * #renderWayPoint(java.awt.Graphics,
	 * org.openstreetmap.josm.plugins.elevation.IElevationProfile,
	 * org.openstreetmap.josm.data.gpx.WayPoint,
	 * org.openstreetmap.josm.plugins.elevation.ElevationWayPointKind)
	 */
	public void renderWayPoint(Graphics g, IElevationProfile profile,
			MapView mv, WayPoint wpt, ElevationWayPointKind kind) {

		if (mv == null || profile == null || wpt == null) {
			System.err.println(String.format(
					"Cannot paint: mv=%s, prof=%s, wpt=%s", mv, profile, wpt));
			return;
		}

		switch (kind) {
		case MinElevation:
		case MaxElevation:
			renderMinMaxPoint(g, profile, mv, wpt, kind);
			break;
		case EndPoint:
		case StartPoint:
			renderStartEndPoint(g, profile, mv, wpt, kind);
			break;
		default:
			renderRegularWayPoint(g, profile, mv, wpt, kind);
			break;
		}
	}

	/**
	 * Renders a regular way point.
	 * 
	 * @param g
	 *            The graphics context.
	 * @param profile
	 *            The elevation profile.
	 * @param mv
	 *            The map view instance.
	 * @param wpt
	 *            The way point to render.
	 * @param kind
	 *            The way point kind (start, end, max,...).
	 */
	private void renderRegularWayPoint(Graphics g, IElevationProfile profile,
			MapView mv, WayPoint wpt, ElevationWayPointKind kind) {

		Color c = getColorForWaypoint(profile, wpt, kind);

		if (c == null) {
			System.err.println(String.format(
					"Cannot determine color: mv=%s, prof=%s, wpt=%s", mv,
					profile, wpt));
		}

		Point pnt = mv.getPoint(wpt.getEastNorth());

		int rad = REGULAR_WPT_RADIUS;
		g.setColor(c);
		//g.drawOval(pnt.x - rad, pnt.y - rad, r2, r2);
		drawSphere(g, Color.WHITE, c, pnt.x, pnt.y, rad);
		
		if (kind == ElevationWayPointKind.FullHour) {
			int hour = WayPointHelper.getHourOfWayPoint(wpt);			
			drawLabel(String.format("%02d:00", hour), pnt.x, pnt.y + g.getFontMetrics().getHeight(), g);
		}
		
		if (kind == ElevationWayPointKind.ElevationLevel) {
			int ele = ((int)Math.rint(WayPointHelper.getElevation(wpt) / 100.0)) * 100;						
			drawLabel(String.format("%dm", ele), pnt.x, pnt.y + g.getFontMetrics().getHeight(), g, c);
		}
		
		if (kind == ElevationWayPointKind.Highlighted) {
			int eleH = (int) WayPointHelper.getElevation(wpt);
			int hour = WayPointHelper.getHourOfWayPoint(wpt);			
			int min = WayPointHelper.getMinuteOfWayPoint(wpt);
			drawSphere(g, Color.WHITE, c, pnt.x, pnt.y, BIG_WPT_RADIUS);
			drawLabel(String.format("%02d:%02d", hour, min), pnt.x, pnt.y - g.getFontMetrics().getHeight() - 5, g);
			drawLabel(String.format("%dm", eleH), pnt.x, pnt.y + g.getFontMetrics().getHeight() + 5, g);
		}
	}

	/**
	 * Renders a min/max point
	 * 
	 * @param g
	 *            The graphics context.
	 * @param profile
	 *            The elevation profile.
	 * @param mv
	 *            The map view instance.
	 * @param wpt
	 *            The way point to render.
	 * @param kind
	 *            The way point kind (start, end, max,...).
	 */
	private void renderMinMaxPoint(Graphics g, IElevationProfile profile,
			MapView mv, WayPoint wpt, ElevationWayPointKind kind) {

		Color c = getColorForWaypoint(profile, wpt, kind);
		int eleH = (int) WayPointHelper.getElevation(wpt);
		Point pnt = mv.getPoint(wpt.getEastNorth());

		TriangleDir td = TriangleDir.Up;

		switch (kind) {
		case MaxElevation:
			td = TriangleDir.Up;
			break;
		case MinElevation:
			td = TriangleDir.Down;
			break;
		case EndPoint:
			td = TriangleDir.Left;
			break;
		case StartPoint:
			td = TriangleDir.Right;
			break;
		default:
			return; // nothing to do
		}

		paintRegularTriangle(g, c, td, pnt.x, pnt.y,
				DefaultElevationProfileRenderer.TRIANGLE_BASESIZE);

		drawLabel(String.format("%dm", eleH), pnt.x, pnt.y + g.getFontMetrics().getHeight(), g, c);
	}

	/**
	 * Draw a regular triangle.
	 * 
	 * @param g
	 *            The graphics context.
	 * @param c
	 *            The fill color of the triangle.
	 * @param dir
	 *            The direction of the the triangle
	 * @param x
	 *            The x coordinate in the graphics context.
	 * @param y
	 *            The y coordinate in the graphics context.
	 * @param baseLength
	 *            The side length in pixel of the triangle.
	 */
	private void paintRegularTriangle(Graphics g, Color c, TriangleDir dir,
			int x, int y, int baseLength) {
		if (baseLength < 2)
			return; // cannot render triangle

		int b2 = baseLength >> 1;

		// coordinates for upwards directed triangle
		Point p[] = new Point[3];

		for (int i = 0; i < p.length; i++) {
			p[i] = new Point();
		}

		p[0].x = -b2;
		p[0].y = b2;

		p[1].x = b2;
		p[1].y = b2;

		p[2].x = 0;
		p[2].y = -b2;

		Triangle t = new Triangle(p[0], p[1], p[2]);

		// rotation angle in rad
		double theta = 0.0;

		switch (dir) {
		case Up:
			theta = 0.0;
			break;
		case Down:
			theta = RAD_180;
			break;
		case Left:
			theta = -RAD_90;
			break;
		case Right:
			theta = RAD_90;
			break;
		}

		// rotate shape
		AffineTransform at = AffineTransform.getRotateInstance(theta);
		Shape tRot = at.createTransformedShape(t);
		// translate shape
		AffineTransform at2 = AffineTransform.getTranslateInstance(x, y);
		Shape ts = at2.createTransformedShape(tRot);

		// draw the shape
		Graphics2D g2 = (Graphics2D) g;
		if (g2 != null) {
			Color oldC = g2.getColor();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(c);
			g2.fill(ts);
			g2.setColor(oldC);
		}
	}

	/**
	 * Renders a start/end point
	 * 
	 * @param g
	 *            The graphics context.
	 * @param profile
	 *            The elevation profile.
	 * @param mv
	 *            The map view instance.
	 * @param wpt
	 *            The way point to render.
	 * @param kind
	 *            The way point kind (start, end, max,...).
	 */
	private void renderStartEndPoint(Graphics g, IElevationProfile profile,
			MapView mv, WayPoint wpt, ElevationWayPointKind kind) {

		Color c = getColorForWaypoint(profile, wpt, kind);
		Point pnt = mv.getPoint(wpt.getEastNorth());
		drawSphere(g, Color.WHITE, c, pnt.x, pnt.y, BIG_WPT_RADIUS);
	}

	/**
	 * Draw a shaded sphere.
	 * 
	 * @param g
	 *            The graphics context.
	 * @param firstCol
	 *            The focus color (usually white).
	 * @param secondCol
	 *            The sphere color.
	 * @param x
	 *            The x coordinate of the sphere center.
	 * @param y
	 *            The y coordinate of the sphere center.
	 * @param radius
	 *            The radius of the sphere.
	 */
	private void drawSphere(Graphics g, Color firstCol, Color secondCol, int x,
			int y, int radius) {
		Point2D center = new Point2D.Float(x, y);
		Point2D focus = new Point2D.Float(x - (radius * 0.6f), y
				- (radius * 0.6f));
		float[] dist = { 0.1f, 0.2f, 1.0f };
		Color[] colors = { firstCol, secondCol, Color.BLACK };
		RadialGradientPaint p = new RadialGradientPaint(center, radius, focus,
				dist, colors, CycleMethod.NO_CYCLE);

		Graphics2D g2 = (Graphics2D) g;
		if (g2 != null) {
			g2.setPaint(p);
			int r2 = radius / 2;
			g2.fillOval(x - r2, y - r2, radius, radius);
		}
	}

	/**
	 * Draws a label.
	 * @param s The text to draw.
	 * @param x The x coordinate of the label.
	 * @param y The y coordinate of the label.
	 * @param g The graphics context.
	 */
	private void drawLabel(String s, int x, int y, Graphics g) {
		drawLabel(s, x, y, g, Color.BLACK);
	}
	/**
	 * Draws a label.
	 * @param s The text to draw.
	 * @param x The x coordinate of the label.
	 * @param y The y coordinate of the label.
	 * @param g The graphics context.
	 * @param secondGradColor The second color of the gradient.
	 */
	private void drawLabel(String s, int x, int y, Graphics g, Color secondGradColor) {
		Graphics2D g2d = (Graphics2D) g;

		int width = g.getFontMetrics(g.getFont()).stringWidth(s) + 10;
		int height = g.getFont().getSize() + g.getFontMetrics().getLeading() + 5;

		Rectangle r = new Rectangle(x - (width / 2), y - (height / 2), width, height);
		
		if (isForbiddenArea(r)) { 
			return; // no space left, skip this label
		}
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);		
		GradientPaint gradient = new GradientPaint(x, y, Color.WHITE, x, y
				+ (height/2), secondGradColor, false);
		g2d.setPaint(gradient);		

		
		g2d.fillRoundRect(r.x, r.y, r.width, r.height, ROUND_RECT_RADIUS, ROUND_RECT_RADIUS);
		
		g2d.setColor(Color.BLACK);
		
		g2d.drawRoundRect(r.x, r.y, r.width, r.height, ROUND_RECT_RADIUS, ROUND_RECT_RADIUS);		
		g2d.drawString(s, x - (width / 2) + 5, y + (height / 2) - 3);
		
		forbiddenRects.add(r);
	}
	
	/**
	 * Checks, if the rectangle has been 'reserved' by a previous draw action.
	 * @param r The area to check for.
	 * @return true; if area is already occupied by another rectangle.
	 */
	private boolean isForbiddenArea(Rectangle r) {
		
		for (Rectangle rTest : forbiddenRects) {
			if (r.intersects(rTest)) return true;
		}
		return false;
	}

	@Override
	public void beginRendering() {
		forbiddenRects.clear();
	}

	@Override
	public void finishRendering() {
		// nothing to do currently		
	}
}
