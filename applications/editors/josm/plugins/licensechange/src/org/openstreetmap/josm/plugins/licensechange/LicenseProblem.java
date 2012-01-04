// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.AbstractVisitor;
import org.openstreetmap.josm.gui.MapView;

/**
 */
public class LicenseProblem 
{
    /** Severity */
    private Severity severity;
    /** The problem message */
    private String message;
    /** The affected primitives */
    private List<? extends OsmPrimitive> primitives;
    /** The primitives to be highlighted */
    private List<?> highlighted;
    /** The tester that raised this error */
    private Check tester;
    /** Whether this is selected */
    private boolean selected;

    /**
     * Constructors
     * @param tester The tester
     * @param severity The severity of this problem
     * @param message The error message
     * @param primitive The affected primitive
     * @param primitives The affected primitives
     */
    public LicenseProblem(Check tester, Severity severity, String message, List<? extends OsmPrimitive> primitives, List<?> highlighted) {
        this.tester = tester;
        this.severity = severity;
        this.message = message;
        this.primitives = primitives;
        this.highlighted = highlighted;
    }

    /**
     * Gets the error message
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message
     * @param message The error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the list of primitives affected by this error
     * @return the list of primitives affected by this error
     */
    public List<? extends OsmPrimitive> getPrimitives() {
        return primitives;
    }

    /**
     * Sets the list of primitives affected by this error
     * @param primitives the list of primitives affected by this error
     */

    public void setPrimitives(List<OsmPrimitive> primitives) {
        this.primitives = primitives;
    }

    /**
     * Gets the severity of this error
     * @return the severity of this error
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity of this error
     * @param severity the severity of this error
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }


    /**
     * Gets the tester that raised this error
     * @return the tester that raised this error
     */
    public Check getTester() {
        return tester;
    }

    /**
     * Paints the error on affected primitives
     *
     * @param g The graphics
     * @param mv The MapView
     */
    public void paint(Graphics g, MapView mv) {
        PaintVisitor v = new PaintVisitor(g, mv);
        visitHighlighted(v);
    }

    @SuppressWarnings("unchecked")
    public void visitHighlighted(LicenseChangeVisitor v) {
        for (Object o : highlighted) {
            if (o instanceof OsmPrimitive)
                v.visit((OsmPrimitive) o);
            else if (o instanceof List<?>) {
                v.visit((List<Node>)o);
            }
        }
    }

    /**
     * Visitor that highlights the primitives affected by this error
     * @author frsantos
     */
    class PaintVisitor extends AbstractVisitor implements LicenseChangeVisitor {
        /** The graphics */
        private final Graphics g;
        /** The MapView */
        private final MapView mv;

        /**
         * Constructor
         * @param g The graphics
         * @param mv The Mapview
         */
        public PaintVisitor(Graphics g, MapView mv) {
            this.g = g;
            this.mv = mv;
        }

        public void visit(OsmPrimitive p) {
            if (p.isUsable()) {
                p.visit(this);
            }
        }

        /**
         * Draws a circle around the node
         * @param n The node
         * @param color The circle color
         */
        public void drawNode(Node n, Color color) {
            Point p = mv.getPoint(n);
            g.setColor(color);
            if (selected) {
                g.fillOval(p.x - 6, p.y - 6, 12, 12);
            } else
                g.drawOval(p.x - 6, p.y - 6, 12, 12);
        }

        public void drawSegment(Point p1, Point p2, boolean linecapStart, boolean linecapEnd, Color color) {
            g.setColor(color);

            double t = Math.atan2(p2.x - p1.x, p2.y - p1.y);
            double cosT = Math.cos(t);
            double sinT = Math.sin(t);
            int deg = (int) Math.toDegrees(t);
            if (selected) {
                int[] x = new int[] { (int) (p1.x + 4 * cosT), (int) (p2.x + 4 * cosT), (int) (p2.x - 4 * cosT),
                        (int) (p1.x - 4 * cosT) };
                int[] y = new int[] { (int) (p1.y - 4 * sinT), (int) (p2.y - 4 * sinT), (int) (p2.y + 4 * sinT),
                        (int) (p1.y + 4 * sinT) };
                g.fillPolygon(x, y, 4);
                if (linecapStart) g.fillArc(p1.x - 4, p1.y - 4, 8, 8, deg, 180);
                if (linecapEnd) g.fillArc(p2.x - 4, p2.y - 4, 8, 8, deg, -180);
            } else {
                g.drawLine((int) (p1.x + 4 * cosT), (int) (p1.y - 4 * sinT), (int) (p2.x + 4 * cosT),
                        (int) (p2.y - 4 * sinT));
                g.drawLine((int) (p1.x - 4 * cosT), (int) (p1.y + 4 * sinT), (int) (p2.x - 4 * cosT),
                        (int) (p2.y + 4 * sinT));
                if (linecapStart) g.drawArc(p1.x - 4, p1.y - 4, 8, 8, deg, 180);
                if (linecapEnd) g.drawArc(p2.x - 4, p2.y - 4, 8, 8, deg, -180);
            }
        }

        /**
         * Draws a line around the segment
         *
         * @param s The segment
         * @param color The color
         */
        public void drawSegment(Node n1, Node n2, boolean linecapStart, boolean linecapEnd, Color color) {
            drawSegment(mv.getPoint(n1), mv.getPoint(n2), linecapStart, linecapEnd, color);
        }

        /**
         * Draw a small rectangle.
         * White if selected (as always) or red otherwise.
         *
         * @param n The node to draw.
         */
        public void visit(Node n) {
            if (isNodeVisible(n))
                drawNode(n, severity.getColor());
        }

        public void visit(Way w) {
            visit(w.getNodes());
        }

        public void visit(Relation r) {
            /* No idea how to draw a relation. */
        }

        /**
         * Checks if the given node is in the visible area.
         * @param n The node to check for visibility
         * @return true if the node is visible
         */
        protected boolean isNodeVisible(Node n) {
            Point p = mv.getPoint(n);
            return !((p.x < 0) || (p.y < 0) || (p.x > mv.getWidth()) || (p.y > mv.getHeight()));
        }

        /**
         * Checks if the given segment is in the visible area.
         * NOTE: This will return true for a small number of non-visible
         *       segments.
         * @param ls The segment to check
         * @return true if the segment is visible
         */
        protected boolean isSegmentVisible(Node n1, Node n2) {
            Point p1 = mv.getPoint(n1);
            Point p2 = mv.getPoint(n2);
            if ((p1.x < 0) && (p2.x < 0))
                return false;
            if ((p1.y < 0) && (p2.y < 0))
                return false;
            if ((p1.x > mv.getWidth()) && (p2.x > mv.getWidth()))
                return false;
            if ((p1.y > mv.getHeight()) && (p2.y > mv.getHeight()))
                return false;
            return true;
        }

        public void visit(List<Node> nodes) {
            Node[] window = new Node[4];
            int index = 1;
            window[0] = null;
            for (Node n : nodes) {
                if (index == 1)
                {
                    window[1] = n;
                    index = 2;
                    continue;
                }
                window[index++] = n;
                if (index==4)
                {
                    drawSegment(window[1], window[2], window[0] == null, !isSegmentVisible(window[2], window[3]), severity.getColor());
                    window[0] = window[1];
                    window[1] = window[2];
                    window[2] = window[3];
                    index = 3;
                }
            }
            if (window[1] != null && window[2] != null) drawSegment(window[1], window[2], window[0] == null, true, severity.getColor());
        }
    }

    /**
     * Sets the selection flag of this error
     * @param selected if this error is selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
