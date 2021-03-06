// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.elevation.ElevationHelper;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.gpx.ElevationWayPointKind;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Logging;

/**
 * Provides default rendering for elevation profile layer.
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public class DefaultElevationProfileRenderer implements
IElevationProfileRenderer {

    private static final int ROUND_RECT_RADIUS = 6;
    /**
     *
     */
    private static final int TRIANGLE_BASESIZE = 24;
    /**
     *
     */
    private static final int BASIC_WPT_RADIUS = 1;
    private static final int BIG_WPT_RADIUS = BASIC_WPT_RADIUS * 16;

    // predefined colors
    private static final Color HIGH_COLOR = ElevationColors.EPMidBlue;
    private static final Color LOW_COLOR = ElevationColors.EPMidBlue;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_POINT = Color.RED;
    private static final Color LEVEL_GAIN_COLOR = Color.GREEN;
    private static final Color LEVEL_LOSS_COLOR = Color.RED;
    private static final Color MARKER_POINT = Color.YELLOW;
    // Predefined radians
    private static final double RAD_180 = Math.PI;
    // private static final double RAD_270 = Math.PI * 1.5;
    private static final double RAD_90 = Math.PI * 0.5;

    private final List<Rectangle> forbiddenRects = new ArrayList<>();

    @Override
    public Color getColorForWaypoint(IElevationProfile profile, WayPoint wpt,
            ElevationWayPointKind kind) {

        if (wpt == null || profile == null) {
            Logging.error(String.format("Cannot determine color: prof=%s, wpt=%s", profile, wpt));
            return null;
        }

        switch (kind) {
        case Plain:
            return Color.LIGHT_GRAY;
        case ElevationLevelLoss:
            return LEVEL_LOSS_COLOR;
        case ElevationLevelGain:
            return LEVEL_GAIN_COLOR;
        case Highlighted:
            return Color.ORANGE;
        case ElevationGainHigh:
            return Color.getHSBColor(0.3f, 1.0f, 1.0f); // green
        case ElevationLossHigh:
            return Color.getHSBColor(0, 1.0f, 1.0f); // red
        case ElevationGainLow:
            return Color.getHSBColor(0.3f, 0.5f, 1.0f); // green with low sat
        case ElevationLossLow:
            return Color.getHSBColor(0, 0.5f, 1.0f); // red with low sat
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
        default:
            break;
        }

        throw new RuntimeException("Unknown way point kind: " + kind);
    }

    @Override
    public void renderWayPoint(Graphics g, IElevationProfile profile,
            MapView mv, WayPoint wpt, ElevationWayPointKind kind) {

        CheckParameterUtil.ensureParameterNotNull(g, "graphics");
        CheckParameterUtil.ensureParameterNotNull(profile, "profile");
        CheckParameterUtil.ensureParameterNotNull(mv, "map view");

        if (wpt == null) {
            Logging.error(String.format("Cannot paint: mv=%s, prof=%s, wpt=%s", mv, profile, wpt));
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

    @Override
    public void renderLine(Graphics g, IElevationProfile profile,
            MapView mv, WayPoint wpt1, WayPoint wpt2, ElevationWayPointKind kind) {

        CheckParameterUtil.ensureParameterNotNull(g, "graphics");
        CheckParameterUtil.ensureParameterNotNull(profile, "profile");
        CheckParameterUtil.ensureParameterNotNull(mv, "map view");

        if (wpt1 == null || wpt2 == null) {
            Logging.error(String.format("Cannot paint line: mv=%s, prof=%s, kind = %s", mv, profile, kind));
            return;
        }

        // obtain and set color
        g.setColor(getColorForWaypoint(profile, wpt2, kind));

        // transform to view
        Point pnt1 = mv.getPoint(wpt1.getEastNorth(ProjectionRegistry.getProjection()));
        Point pnt2 = mv.getPoint(wpt2.getEastNorth(ProjectionRegistry.getProjection()));

        // use thick line, if possible
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke oldS = g2.getStroke();
            try {
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(pnt1.x, pnt1.y, pnt2.x, pnt2.y);
            } finally {
                // must be restored; otherwise other layers may using this style, too
                g2.setStroke(oldS);
            }
        } else {
            // only poor man's graphics
            g.drawLine(pnt1.x, pnt1.y, pnt2.x, pnt2.y);
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
        Point pnt = mv.getPoint(wpt.getEastNorth(ProjectionRegistry.getProjection()));

        /* Paint full hour label */
        if (kind == ElevationWayPointKind.FullHour) {
            int hour = ElevationHelper.getHourOfWayPoint(wpt);
            drawLabel(String.format("%02d:00", hour), pnt.x, pnt.y
                    + g.getFontMetrics().getHeight(), g);
        }

        /* Paint label for elevation levels */
        if (kind == ElevationWayPointKind.ElevationLevelGain || kind == ElevationWayPointKind.ElevationLevelLoss) {
            int ele = ((int) Math.rint(ElevationHelper.getElevation(wpt) / 100.0)) * 100;
            drawLabelWithTriangle(ElevationHelper.getElevationText(ele), pnt.x, pnt.y
                    + g.getFontMetrics().getHeight(), g, Color.darkGray, 8,
                    getColorForWaypoint(profile, wpt, kind),
                    kind == ElevationWayPointKind.ElevationLevelGain ? TriangleDir.Up : TriangleDir.Down);
        }

        /* Paint cursor labels */
        if (kind == ElevationWayPointKind.Highlighted) {
            drawSphere(g, Color.WHITE, c, pnt.x, pnt.y, BIG_WPT_RADIUS);
            drawLabel(ElevationHelper.getTimeText(wpt), pnt.x, pnt.y
                    - g.getFontMetrics().getHeight() - 5, g);
            drawLabel(ElevationHelper.getElevationText(wpt), pnt.x, pnt.y
                    + g.getFontMetrics().getHeight() + 5, g);
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
        int eleH = (int) ElevationHelper.getElevation(wpt);
        Point pnt = mv.getPoint(wpt.getEastNorth(ProjectionRegistry.getProjection()));

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

        drawRegularTriangle(g, c, td, pnt.x, pnt.y,
                DefaultElevationProfileRenderer.TRIANGLE_BASESIZE);

        drawLabel(ElevationHelper.getElevationText(eleH), pnt.x, pnt.y
                + g.getFontMetrics().getHeight(), g, c);
    }

    /**
     * Draws a regular triangle.
     *
     * @param g
     *            The graphics context.
     * @param c
     *            The fill color of the triangle.
     * @param dir
     *            The direction of the triangle
     * @param x
     *            The x coordinate in the graphics context.
     * @param y
     *            The y coordinate in the graphics context.
     * @param baseLength
     *            The side length in pixel of the triangle.
     */
    private void drawRegularTriangle(Graphics g, Color c, TriangleDir dir,
            int x, int y, int baseLength) {
        if (baseLength < 2)
            return; // cannot render triangle

        int b2 = baseLength >> 1;

        // coordinates for upwards directed triangle
        Point[] p = new Point[3];

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
     * Renders a start/end point.
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
        Point pnt = mv.getPoint(wpt.getEastNorth(ProjectionRegistry.getProjection()));
        drawSphere(g, Color.WHITE, c, pnt.x, pnt.y, BIG_WPT_RADIUS);
    }

    /**
     * Draws a shaded sphere.
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
        float[] dist = {0.1f, 0.2f, 1.0f};
        Color[] colors = {firstCol, secondCol, Color.DARK_GRAY};
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
     * Draws a label within a filled rounded rectangle with standard gradient colors.
     *
     * @param s
     *            The text to draw.
     * @param x
     *            The x coordinate of the label.
     * @param y
     *            The y coordinate of the label.
     * @param g
     *            The graphics context.
     */
    private void drawLabel(String s, int x, int y, Graphics g) {
        drawLabel(s, x, y, g, Color.GRAY);
    }

    /**
     * Draws a label within a filled rounded rectangle with the specified second gradient color (first color is <tt>Color.WHITE</tt>).
     *
     * @param s
     *            The text to draw.
     * @param x
     *            The x coordinate of the label.
     * @param y
     *            The y coordinate of the label.
     * @param g
     *            The graphics context.
     * @param secondGradColor
     *            The second color of the gradient.
     */
    private void drawLabel(String s, int x, int y, Graphics g,
            Color secondGradColor) {
        Graphics2D g2d = (Graphics2D) g;

        int width = g.getFontMetrics(g.getFont()).stringWidth(s) + 10;
        int height = g.getFont().getSize() + g.getFontMetrics().getLeading()
                + 5;

        Rectangle r = new Rectangle(x - (width / 2), y - (height / 2), width,
                height);

        if (isForbiddenArea(r)) {
            return; // no space left, skip this label
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(x, y, Color.WHITE, x, y
                + (height / 2), secondGradColor, false);
        g2d.setPaint(gradient);

        g2d.fillRoundRect(r.x, r.y, r.width, r.height, ROUND_RECT_RADIUS,
                ROUND_RECT_RADIUS);

        g2d.setColor(Color.BLACK);

        g2d.drawRoundRect(r.x, r.y, r.width, r.height, ROUND_RECT_RADIUS,
                ROUND_RECT_RADIUS);
        g2d.drawString(s, x - (width / 2) + 5, y + (height / 2) - 3);

        forbiddenRects.add(r);
    }

    /**
     * Draws a label with an additional triangle on the left side.
     *
     * @param s
     *            The text to draw.
     * @param x
     *            The x coordinate of the label.
     * @param y
     *            The y coordinate of the label.
     * @param g
     *            The graphics context.
     * @param secondGradColor
     *            The second color of the gradient.
     * @param baseLength
     *            The base length of the triangle in pixels.
     * @param triangleColor
     *            The color of the triangle.
     * @param triangleDir
     *            The direction of the triangle.
     */
    private void drawLabelWithTriangle(String s, int x, int y, Graphics g,
            Color secondGradColor, int baseLength, Color triangleColor,
            TriangleDir triangleDir) {
        Graphics2D g2d = (Graphics2D) g;

        int width = g.getFontMetrics(g.getFont()).stringWidth(s) + 10 + baseLength + 5;
        int height = g.getFont().getSize() + g.getFontMetrics().getLeading() + 5;

        Rectangle r = new Rectangle(x - (width / 2), y - (height / 2), width, height);

        if (isForbiddenArea(r)) {
            return; // no space left, skip this label
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(x, y, Color.WHITE, x, y
                + (height / 2), secondGradColor, false);
        g2d.setPaint(gradient);

        g2d.fillRoundRect(r.x, r.y, r.width, r.height, ROUND_RECT_RADIUS,
                ROUND_RECT_RADIUS);

        g2d.setColor(Color.BLACK);

        g2d.drawRoundRect(r.x, r.y, r.width, r.height, ROUND_RECT_RADIUS,
                ROUND_RECT_RADIUS);
        g2d.drawString(s, x - (width / 2) + 8 + baseLength, y + (height / 2) - 3);
        drawRegularTriangle(g2d, triangleColor, triangleDir, r.x + baseLength,
                r.y + baseLength, baseLength);

        forbiddenRects.add(r);
    }

    /**
     * Checks, if the rectangle has been 'reserved' by an previous draw action.
     *
     * @param r
     *            The area to check for.
     * @return true, if area is already occupied by another rectangle.
     */
    private boolean isForbiddenArea(Rectangle r) {

        for (Rectangle rTest : forbiddenRects) {
            if (r.intersects(rTest))
                return true;
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
