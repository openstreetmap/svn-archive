package org.openstreetmap.josm.plugins.turnlanes.gui;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.angle;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.cpf;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.minAngleDiff;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.relativePoint;

import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.NoSuchElementException;

/**
 * A path that allows constructing offset curves/parallel curves with a somewhat crude straight
 * skeleton implementation.
 * 
 * @author Ben Schulz
 */
abstract class Path {
    private static final class SimplePathIterator implements PathIterator {
        private final SimplePathIterator previous;
        
        private final int type;
        private final double[] coords;
        
        private boolean done = false;
        
        public SimplePathIterator(SimplePathIterator previous, int type, double... coords) {
            this.previous = previous;
            this.type = type;
            this.coords = coords;
        }
        
        public SimplePathIterator(int type, double... coords) {
            this(null, type, coords);
        }
        
        @Override
        public int getWindingRule() {
            return WIND_NON_ZERO;
        }
        
        @Override
        public boolean isDone() {
            return done;
        }
        
        @Override
        public void next() {
            if (previous != null && !previous.isDone()) {
                previous.next();
            } else {
                done = true;
            }
        }
        
        @Override
        public int currentSegment(float[] coords) {
            if (previous != null && !previous.isDone()) {
                return previous.currentSegment(coords);
            } else if (done) {
                throw new NoSuchElementException("Iterator is already done.");
            }
            
            for (int i = 0; i < 6; ++i) {
                coords[i] = (float) this.coords[i];
            }
            
            return type;
        }
        
        @Override
        public int currentSegment(double[] coords) {
            if (previous != null && !previous.isDone()) {
                return previous.currentSegment(coords);
            } else if (done) {
                throw new NoSuchElementException("Iterator is already done.");
            }
            
            for (int i = 0; i < 6; ++i) {
                coords[i] = this.coords[i];
            }
            
            return type;
        }
        
    }
    
    private static final class Line extends Path {
        private final Path previous;
        
        private final double endX;
        private final double endY;
        
        private final double angle;
        
        private final double length;
        
        public Line(Path previous, double x, double y, double length) {
            this.previous = previous;
            
            this.endX = x;
            this.endY = y;
            
            this.angle = angle(previous.getEnd(), getEnd());
            
            this.length = length;
        }
        
        @Override
        public Point2D getStart() {
            return previous.getStart();
        }
        
        @Override
        public Point2D getEnd() {
            return new Point2D.Double(endX, endY);
        }
        
        @Override
        public double getEndAngle() {
            return angle;
        }
        
        @Override
        public double getLength() {
            return previous.getLength() + length;
        }
        
        @Override
        public Path offset(double ws, double m1, double m2, double we) {
            return offsetInternal(ws, m1, m2, we, angle);
        }
        
        @Override
        Path offsetInternal(double ws, double m1, double m2, double we, double endAngle) {
            final double PL = previous.getLength();
            final double ML = PL + length;
            
            final Path prev = previous.offsetInternal(ws, m1, m2, we, angle);
            
            final double wStart = PL <= m1 ? ws : m2 <= PL ? we : ws + (PL - m1) * (we - ws) / (m2 - m1);
            final Point2D from = prev.getEnd();
            final Point2D to = offsetEnd(wStart, endAngle);
            
            if (abs(minAngleDiff(angle, angle(from, to))) > PI / 100) {
                return previous.offsetInternal(ws, m1, m2, we, endAngle);
            }
            
            if (ML <= m1) {
                return simpleOffset(prev, ws, endAngle);
            } else if (m2 <= PL) {
                return simpleOffset(prev, we, endAngle);
            }
            
            final double LL = from.distance(to);
            
            final Point2D m1o = PL <= m1 ? relativePoint(prev.getEnd(), LL * (m1 - PL) / length, angle) : null;
            final Point2D m2t = m2 <= ML ? relativePoint(getEnd(), LL * (ML - m2) / length, angle + PI) : null;
            final Point2D m2o = m2t == null ? null : relativePoint(m2t, we, (angle + endAngle - PI) / 2);
            
            if (m1o != null && m2o != null) {
                final Line l1 = new Line(prev, m1o.getX(), m1o.getY(), m1 - PL);
                final Line l2 = new Line(l1, m2o.getX(), m2o.getY(), m2 - m1);
                
                final Point2D end = offsetEnd(we, endAngle);
                
                return new Line(l2, end.getX(), end.getY(), ML - m2);
            } else if (m1o != null) {
                final Line l1 = new Line(prev, m1o.getX(), m1o.getY(), m1 - PL);
                
                final double w = ws + (ML - m1) * (we - ws) / (m2 - m1);
                final Point2D end = offsetEnd(w, endAngle);
                
                return new Line(l1, end.getX(), end.getY(), ML - m1);
            } else if (m2o != null) {
                final Line l2 = new Line(prev, m2o.getX(), m2o.getY(), m2 - PL);
                
                final Point2D end = offsetEnd(we, endAngle);
                
                return new Line(l2, end.getX(), end.getY(), ML - m2);
            } else {
                final double w = ws + (PL - m1 + length) * (we - ws) / (m2 - m1);
                final Point2D end = offsetEnd(w, endAngle);
                return new Line(prev, end.getX(), end.getY(), length);
            }
        }
        
        private Path simpleOffset(Path prev, double w, double endAngle) {
            final Point2D offset = offsetEnd(w, endAngle);
            return new Line(prev, offset.getX(), offset.getY(), length);
        }
        
        private Point2D offsetEnd(double w, double endAngle) {
            final double da2 = minAngleDiff(angle, endAngle) / 2;
            final double hypotenuse = w / cos(da2);
            
            return relativePoint(getEnd(), hypotenuse, angle + PI / 2 + da2);
        }
        
        @Override
        public SimplePathIterator getIterator() {
            return new SimplePathIterator(previous.getIteratorInternal(angle), PathIterator.SEG_LINETO, endX, endY, 0,
                    0, 0, 0);
        }
        
        @Override
        public Path subpath(double from, double to) {
            final double PL = previous.getLength();
            final double ML = PL + length;
            
            if (from > ML) {
                throw new IllegalArgumentException("from > length");
            } else if (to > ML) {
                throw new IllegalArgumentException("to > length");
            }
            
            if (to < PL) {
                return previous.subpath(from, to);
            }
            
            final Point2D end = to < ML ? getPoint(to) : new Point2D.Double(endX, endY);
            
            final double EL = min(ML, to);
            if (PL <= from) {
                final Point2D start = getPoint(from);
                return new Line(new Start(start.getX(), start.getY(), angle), end.getX(), end.getY(), EL - from);
            } else {
                return new Line(previous.subpath(from, PL), end.getX(), end.getY(), EL - PL);
            }
        }
        
        @Override
        public Point2D getPoint(double offset) {
            final double PL = previous.getLength();
            final double ML = PL + length;
            
            if (offset > ML) {
                throw new IllegalArgumentException("offset > length");
            }
            
            if (offset <= ML && offset >= PL) {
                final double LL = previous.getEnd().distance(getEnd());
                return relativePoint(getEnd(), LL * (ML - offset) / length, angle + PI);
            } else {
                return previous.getPoint(offset);
            }
        }
        
        @Override
        SimplePathIterator getIteratorInternal(double endAngle) {
            return getIterator();
        }
    }
    
    // TODO curves are still somewhat broken
    private static class Curve extends Path {
        private final Path previous;
        
        private final double height;
        
        private final double centerX;
        private final double centerY;
        private final double centerToFromAngle;
        
        private final double endX;
        private final double endY;
        
        private final double fromAngle;
        private final double fromRadius;
        private final double toRadius;
        private final double angle;
        
        private final double length;
        
        private Curve(Path previous, double r1, double r2, double a, double length, double fromAngle) {
            this.previous = previous;
            this.fromAngle = fromAngle;
            this.fromRadius = r1;
            this.toRadius = r2;
            this.angle = a;
            this.length = length;
            
            final Point2D from = previous.getEnd();
            this.centerToFromAngle = fromAngle - signum(a) * PI / 2;
            final Point2D center = relativePoint(from, r1, centerToFromAngle + PI);
            
            final double toAngle = centerToFromAngle + a;
            this.endX = center.getX() + r2 * cos(toAngle);
            this.endY = center.getY() - r2 * sin(toAngle);
            
            this.centerX = center.getX();
            this.centerY = center.getY();
            
            final double y = new Line2D.Double(center, from).ptLineDist(endX, endY);
            this.height = y / sin(angle);
        }
        
        public Curve(Path previous, double r1, double r2, double a, double length) {
            this(previous, r1, r2, a, length, previous.getEndAngle());
        }
        
        public Point2D getStart() {
            return previous.getStart();
        }
        
        @Override
        public Point2D getEnd() {
            return new Point2D.Double(endX, endY);
        }
        
        @Override
        public double getEndAngle() {
            return fromAngle + angle;
        }
        
        @Override
        public double getLength() {
            return previous.getLength() + length;
        }
        
        @Override
        public Path offset(double ws, double m1, double m2, double we) {
            return offsetInternal(ws, m1, m2, we, previous.getEndAngle() + angle);
        }
        
        @Override
        Path offsetInternal(double ws, double m1, double m2, double we, double endAngle) {
            final double PL = previous.getLength();
            final double ML = PL + length;
            
            final Path prev = previous.offsetInternal(ws, m1, m2, we, fromAngle);
            
            if (ML <= m1) {
                return simpleOffset(prev, ws);
            } else if (m2 <= PL) {
                return simpleOffset(prev, we);
            }
            
            final double s = signum(angle);
            
            if (PL < m1 && m2 < ML) {
                final double l1 = m1 - PL;
                final double a1 = angle(l1);
                final double r1 = radius(a1) - s * ws;
                
                final Curve c1 = new Curve(prev, fromRadius - ws, r1, offsetAngle(prev, a1), l1, fromAngle);
                
                final double l2 = m2 - m1;
                final double a2 = angle(l2);
                final double r2 = radius(a2) - s * we;
                
                final Curve c2 = new Curve(c1, r1, r2, a2 - a1, l2);
                
                return new Curve(c2, r2, toRadius - s * we, angle - a2, ML - m2);
            } else if (PL < m1) {
                final double l1 = m1 - PL;
                final double a1 = angle(l1);
                final double r1 = radius(a1) - s * ws;
                
                final Curve c1 = new Curve(prev, fromRadius - s * ws, r1, offsetAngle(prev, a1), l1, fromAngle);
                
                final double w = ws + (ML - m1) * (we - ws) / (m2 - m1);
                
                return new Curve(c1, r1, toRadius - s * w, angle - a1, ML - m1);
            } else if (m2 < ML) {
                final double w = ws + (PL - m1) * (we - ws) / (m2 - m1);
                
                final double l2 = m2 - PL;
                final double a2 = angle(l2);
                final double r2 = radius(a2) - s * we;
                
                final Curve c2 = new Curve(prev, fromRadius - s * w, r2, offsetAngle(prev, a2), l2, fromAngle);
                
                return new Curve(c2, r2, toRadius - s * we, angle - a2, ML - m2);
            } else {
                final double w1 = ws + (PL - m1) * (we - ws) / (m2 - m1);
                final double w2 = we - (m2 - ML) * (we - ws) / (m2 - m1);
                
                return new Curve(prev, fromRadius - s * w1, toRadius - s * w2, offsetAngle(prev, angle), length,
                        fromAngle);
            }
        }
        
        private double angle(double l) {
            return l * angle / length;
        }
        
        private double radius(double a) {
            return hypot(fromRadius * cos(a), height * sin(a));
        }
        
        private double offsetAngle(Path prev, double a) {
            return a;// + GuiUtil.normalize(previous.getEndAngle()
            // - prev.getEndAngle());
        }
        
        private Path simpleOffset(Path prev, double w) {
            final double s = signum(angle);
            return new Curve(prev, fromRadius - s * w, toRadius - s * w, offsetAngle(prev, angle), length, fromAngle);
        }
        
        @Override
        public SimplePathIterator getIterator() {
            return getIteratorInternal(previous.getEndAngle() + angle);
        }
        
        @Override
        public Path subpath(double from, double to) {
            final double PL = previous.getLength();
            final double ML = PL + length;
            
            if (from > ML) {
                throw new IllegalArgumentException("from > length");
            } else if (to > ML) {
                throw new IllegalArgumentException("to > length");
            }
            
            if (to < PL) {
                return previous.subpath(from, to);
            }
            
            final double toA = to < ML ? angle(to - PL) : angle;
            final double toR = to < ML ? radius(toA) : toRadius;
            
            final double fromA = from > PL ? angle(from - PL) : 0;
            final double fromR = from > PL ? radius(fromA) : fromRadius;
            
            final double a = toA - fromA;
            final double l = min(ML, to) - max(PL, from);
            
            if (from >= PL) {
                final Point2D start = getPoint(from);
                final double fa = fromAngle + fromA;
                
                return new Curve(new Start(start.getX(), start.getY(), fa), fromR, toR, a, l, fa);
            } else {
                return new Curve(previous.subpath(from, PL), fromR, toR, a, l, fromAngle);
            }
        }
        
        @Override
        public Point2D getPoint(double offset) {
            final double PL = previous.getLength();
            final double ML = PL + length;
            
            if (offset <= ML && offset >= PL) {
                final double a = abs(angle(offset - PL));
                final double w = fromRadius * cos(a);
                final double h = -height * sin(a);
                
                final double r = centerToFromAngle; // rotation angle
                final double x = w * cos(r) + h * sin(r);
                final double y = -w * sin(r) + h * cos(r);
                
                return new Point2D.Double(centerX + x, centerY + y);
            } else {
                return previous.getPoint(offset);
            }
        }
        
        @Override
        SimplePathIterator getIteratorInternal(double endAngle) {
            final Point2D cp1 = relativePoint(previous.getEnd(), cpf(angle, fromRadius), previous.getEndAngle());
            final Point2D cp2 = relativePoint(getEnd(), cpf(angle, toRadius), endAngle + PI);
            
            return new SimplePathIterator(previous.getIteratorInternal(getEndAngle()), PathIterator.SEG_CUBICTO, //
                    cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(), endX, endY //
            );
            
        }
    }
    
    private static class Start extends Path {
        private final double x;
        private final double y;
        
        private final double endAngle;
        
        public Start(double x, double y, double endAngle) {
            this.x = x;
            this.y = y;
            this.endAngle = endAngle;
        }
        
        public Start(double x, double y) {
            this(x, y, Double.NaN);
        }
        
        public Point2D getStart() {
            return new Point2D.Double(x, y);
        }
        
        public Point2D getEnd() {
            return new Point2D.Double(x, y);
        }
        
        @Override
        public double getEndAngle() {
            if (Double.isNaN(endAngle)) {
                throw new UnsupportedOperationException();
            }
            
            return endAngle;
        }
        
        @Override
        public double getLength() {
            return 0;
        }
        
        @Override
        public Path offset(double ws, double m1, double m2, double we) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        Path offsetInternal(double ws, double m1, double m2, double we, double endAngle) {
            final Point2D offset = relativePoint(getStart(), ws, endAngle + PI / 2);
            return new Start(offset.getX(), offset.getY(), endAngle);
        }
        
        @Override
        public SimplePathIterator getIterator() {
            return new SimplePathIterator(PathIterator.SEG_MOVETO, x, y, 0, 0, 0, 0);
        }
        
        @Override
        public Path subpath(double from, double to) {
            if (from > to) {
                throw new IllegalArgumentException("from > to");
            }
            if (from < 0) {
                throw new IllegalArgumentException("from < 0");
            }
            
            return this;
        }
        
        @Override
        public Point2D getPoint(double offset) {
            if (offset == 0) {
                return getEnd();
            } else {
                throw new IllegalArgumentException(Double.toString(offset));
            }
        }
        
        @Override
        SimplePathIterator getIteratorInternal(double endAngle) {
            return new SimplePathIterator(PathIterator.SEG_MOVETO, x, y, 0, 0, 0, 0);
        }
    }
    
    public static Path create(double x, double y) {
        return new Start(x, y);
    }
    
    public Path lineTo(double x, double y, double length) {
        return new Line(this, x, y, length);
    }
    
    public Path curveTo(double r1, double r2, double a, double length) {
        return new Curve(this, r1, r2, a, length);
    }
    
    public abstract Path offset(double ws, double m1, double m2, double we);
    
    abstract Path offsetInternal(double ws, double m1, double m2, double we, double endAngle);
    
    public abstract double getLength();
    
    public abstract double getEndAngle();
    
    public abstract Point2D getStart();
    
    public abstract Point2D getEnd();
    
    public abstract SimplePathIterator getIterator();
    
    abstract SimplePathIterator getIteratorInternal(double endAngle);
    
    public abstract Path subpath(double from, double to);
    
    public Path subpath(double from, double to, boolean fixArgs) {
        if (fixArgs) {
            from = min(max(from, 0), getLength());
            to = min(max(to, 0), getLength());
        }
        
        return subpath(from, to);
    }
    
    public abstract Point2D getPoint(double offset);
    
    public Point2D getPoint(double offset, boolean fixArgs) {
        if (fixArgs) {
            offset = min(max(offset, 0), getLength());
        }
        
        return getPoint(offset);
    }
}
