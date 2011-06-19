package org.openstreetmap.josm.plugins.turnlanes.gui;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.turnlanes.model.Junction;

class GuiUtil {
    static double normalize(double a) {
        while (a < 0) {
            a += 2 * Math.PI;
        }
        while (a > 2 * Math.PI) {
            a -= 2 * Math.PI;
        }
        return a;
    }
    
    // control point factor for curves (circle segment of angle a)
    static double cpf(double a, double scale) {
        return 4.0 / 3 * Math.tan(min(abs(a), PI - 0.001) / 4) * scale;
    }
    
    static Point2D intersection(Line2D a, Line2D b) {
        final double aa = GuiUtil.angle(a);
        final double ab = GuiUtil.angle(b);
        
        // less than 1/2 degree => no intersection
        if (Math.abs(Math.PI - abs(minAngleDiff(aa, ab))) < PI / 360) {
            return null;
        }
        
        final double d = (a.getX1() - a.getX2()) * (b.getY1() - b.getY2()) - (a.getY1() - a.getY2())
            * (b.getX1() - b.getX2());
        
        final double x = ((b.getX1() - b.getX2()) * (a.getX1() * a.getY2() - a.getY1() * a.getX2()) - (a.getX1() - a
            .getX2()) * (b.getX1() * b.getY2() - b.getY1() * b.getX2()))
            / d;
        final double y = ((b.getY1() - b.getY2()) * (a.getX1() * a.getY2() - a.getY1() * a.getX2()) - (a.getY1() - a
            .getY2()) * (b.getX1() * b.getY2() - b.getY1() * b.getX2()))
            / d;
        
        return new Point2D.Double(x, y);
    }
    
    static Point2D closest(Line2D l, Point2D p) {
        final Point2D lv = vector(l.getP1(), l.getP2());
        final double numerator = dot(vector(l.getP1(), p), lv);
        
        if (numerator < 0) {
            return l.getP1();
        }
        
        final double denominator = dot(lv, lv);
        if (numerator >= denominator) {
            return l.getP2();
        }
        
        final double r = numerator / denominator;
        return new Point2D.Double(l.getX1() + r * lv.getX(), l.getY1() + r * lv.getY());
    }
    
    private static double dot(Point2D a, Point2D b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }
    
    private static Point2D vector(Point2D from, Point2D to) {
        return new Point2D.Double(to.getX() - from.getX(), to.getY() - from.getY());
    }
    
    public static double angle(Point2D from, Point2D to) {
        final double dx = to.getX() - from.getX();
        final double dy = -(to.getY() - from.getY());
        
        return normalize(Math.atan2(dy, dx));
    }
    
    public static Point2D relativePoint(Point2D p, double r, double a) {
        return new Point2D.Double( //
            p.getX() + r * Math.cos(a), //
            p.getY() - r * Math.sin(a) //
        );
    }
    
    public static Line2D relativeLine(Line2D l, double r, double a) {
        final double dx = r * Math.cos(a);
        final double dy = -r * Math.sin(a);
        
        return new Line2D.Double( //
            l.getX1() + dx, //
            l.getY1() + dy, //
            l.getX2() + dx, //
            l.getY2() + dy //
        );
    }
    
    public static double angle(Line2D l) {
        return angle(l.getP1(), l.getP2());
    }
    
    public static double minAngleDiff(double a1, double a2) {
        final double d = normalize(a2 - a1);
        
        return d > Math.PI ? -(2 * Math.PI - d) : d;
    }
    
    public static final Point2D middle(Point2D a, Point2D b) {
        return relativePoint(a, a.distance(b) / 2, angle(a, b));
    }
    
    public static final Point2D middle(Line2D l) {
        return middle(l.getP1(), l.getP2());
    }
    
    public static Line2D line(Point2D p, double a) {
        return new Line2D.Double(p, relativePoint(p, 1, a));
    }
    
    public static Point2D loc(Node node) {
        final EastNorth loc = Main.getProjection().latlon2eastNorth(node.getCoor());
        return new Point2D.Double(loc.getX(), -loc.getY());
    }
    
    public static List<Point2D> locs(Iterable<Junction> junctions) {
        final List<Point2D> locs = new ArrayList<Point2D>();
        
        for (Junction j : junctions) {
            locs.add(loc(j.getNode()));
        }
        
        return locs;
    }
    
    static void area(Path2D area, Path inner, Path outer) {
        area.append(inner.getIterator(), false);
        area.append(ReversePathIterator.reverse(outer.getIterator()), true);
        area.closePath();
    }
}
