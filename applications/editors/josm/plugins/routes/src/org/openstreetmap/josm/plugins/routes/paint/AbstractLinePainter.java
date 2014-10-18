package org.openstreetmap.josm.plugins.routes.paint;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.visitor.paint.LineClip;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;

public abstract class AbstractLinePainter implements PathPainter {

    // Following two method copied from http://blog.persistent.info/2004/03/java-lineline-intersections.html
    protected boolean getLineLineIntersection(Line2D.Double l1,
            Line2D.Double l2,
            Point intersection)
    {
        double  x1 = l1.getX1(), y1 = l1.getY1(),
        x2 = l1.getX2(), y2 = l1.getY2(),
        x3 = l2.getX1(), y3 = l2.getY1(),
        x4 = l2.getX2(), y4 = l2.getY2();
        double dx1 = x2 - x1;
        double dx2 = x4 - x3;
        double dy1 = y2 - y1;
        double dy2 = y4 - y3;

        double ua = (dx2 * (y1 - y3) - dy2 * (x1 - x3)) / (dy2 * dx1 - dx2 * dy1);

        if (Math.abs(dy2 * dx1 - dx2 * dy1) < 0.0001) {
            intersection.x = (int)l1.x2;
            intersection.y = (int)l1.y2;
            return false;
        } else {
            intersection.x = (int)(x1 + ua * (x2 - x1));
            intersection.y = (int)(y1 + ua * (y2 - y1));
        }

        return true;
    }

    protected double det(double a, double b, double c, double d)
    {
        return a * d - b * c;
    }

    protected Point shiftPoint(Point2D p1, Point2D p2, double shift) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();

        // Perpendicular vector
        double ndx = -dy;
        double ndy = dx;

        // Normalize
        double length = Math.sqrt(ndx * ndx + ndy * ndy);
        ndx = ndx / length;
        ndy = ndy / length;

        return new Point((int)(p1.getX() + shift * ndx), (int)(p1.getY() + shift * ndy));
    }

    protected Line2D.Double shiftLine(Point2D p1, Point2D p2, double shift) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();

        Point2D point1 = shiftPoint(p1, p2, shift);
        Point2D point2 = new Point2D.Double(point1.getX() + dx, point1.getY() + dy);

        return new Line2D.Double(
                point1, point2);
    }

    protected GeneralPath getPath(Graphics2D g, MapView mapView, List<Node> nodes, double shift) {

        GeneralPath path = new GeneralPath();

        if (nodes.size() < 2) {
            return path;
        }

        Point p1 = null;
        Point p2 = null;
        Point p3 = null;
        Point lastPoint = null;

        for (Node n: nodes) {
            Point p = mapView.getPoint(n);

            if (!p.equals(p3)) {
                p1 = p2;
                p2 = p3;
                p3 = p;
            } else {
                continue;
            }

            p = null;
            if (p2 != null) {
                if (p1 == null) {
                    p = shiftPoint(p2, p3, shift);
                } else {
                    Line2D.Double line1 = shiftLine(p1, p2, shift);
                    Line2D.Double line2 = shiftLine(p2, p3, shift);

                    /*path.moveTo((float)line1.x1, (float)line1.y1);
                    path.lineTo((float)line1.x2, (float)line1.y2);
                    path.moveTo((float)line2.x1, (float)line2.y1);
                    path.lineTo((float)line2.x2, (float)line2.y2);*/

                    p = new Point();
                    if (!getLineLineIntersection(line1, line2, p)) {
                        p = null;
                    } else {
                        int dx = p.x - p2.x;
                        int dy = p.y - p2.y;
                        int distance = (int)Math.sqrt(dx * dx + dy * dy);
                        if (distance > 10) {
                            p.x = p2.x + dx / (distance / 10);
                            p.y = p2.y + dy / (distance / 10);
                        }
                    }
                }
            }

            if (p != null && lastPoint != null) {
                drawSegment(g, mapView, path, lastPoint, p);
            }
            if (p != null) {
                lastPoint = p;
            }
        }

        if (p2 != null && p3 != null && lastPoint != null) {
            p3 = shiftPoint(p3, p2, -shift);
            drawSegment(g, mapView, path, lastPoint, p3);
        }

        return path;
    }

    private void drawSegment(Graphics2D g, NavigatableComponent nc, GeneralPath path, Point p1, Point p2) {
        boolean drawIt = false;
        if (Main.isOpenjdk) {
            /**
             * Work around openjdk bug. It leads to drawing artefacts when zooming in a lot. (#4289, #4424)
             * (It looks like int overflow when clipping.) We do custom clipping.
             */
            Rectangle bounds = g.getClipBounds();
            bounds.grow(100, 100);                  // avoid arrow heads at the border
            LineClip clip = new LineClip(p1, p2, bounds);
            drawIt = clip.execute();
            if (drawIt) {
                p1 = clip.getP1();
                p2 = clip.getP2();
            }
        } else {
            drawIt = isSegmentVisible(nc, p1, p2);
        }
        if (drawIt) {
            /* draw segment line */
            path.moveTo(p1.x, p1.y);
            path.lineTo(p2.x, p2.y);
        }
    }

    private boolean isSegmentVisible(NavigatableComponent nc, Point p1, Point p2) {
        if ((p1.x < 0) && (p2.x < 0)) return false;
        if ((p1.y < 0) && (p2.y < 0)) return false;
        if ((p1.x > nc.getWidth()) && (p2.x > nc.getWidth())) return false;
        if ((p1.y > nc.getHeight()) && (p2.y > nc.getHeight())) return false;
        return true;
    }


}
