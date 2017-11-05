/**
 *
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.tools.Logging;

/**
 * @author tilusnet <tilusnet@gmail.com>
 *
 */
public class AlignWaysSegment implements MapViewPaintable {

    protected WaySegment segment;
    protected MapView mapview;
    protected Color segmentColor = Color.WHITE;
    protected Collection<Node> segmentEndPoints;

    public AlignWaysSegment(MapView mapview, Point p) throws IllegalArgumentException {
        if (mapview == null)
            throw new IllegalArgumentException(tr(
                    "Parameter ''{0}'' must not be null", "mapview"));
        if (p == null)
            throw new IllegalArgumentException(tr(
                    "Parameter ''{0}'' must not be null", "p"));

        this.mapview = mapview;
    }

    void setSegment(WaySegment segment) {
        this.segment = segment;
        if (segment != null) {
            setSegmentEndpoints(segment);
            mapview.addTemporaryLayer(this);
        }
    }


    void setSegmentEndpoints(WaySegment segment) {
        if (segment != null) {
            try {
                segmentEndPoints = new HashSet<>();
                segmentEndPoints.add(segment.getFirstNode());
                segmentEndPoints.add(segment.getSecondNode());
            } catch (IndexOutOfBoundsException e) {
                Logging.error(e);
            }
        }
    }

    protected WaySegment getNearestWaySegment(Point p) {
        return mapview.getNearestWaySegment(p, OsmPrimitive::isUsable);
    }

    public void destroy() {
        if (segment != null) {
            mapview.removeTemporaryLayer(this);
        }
    }

    public WaySegment getSegment() {
        return segment;
    }

    public Collection<Node> getSegmentEndPoints() {
        return segmentEndPoints;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        // Note: segment should never be null here, and its nodes should never be missing.
        // If they are, it's a bug, possibly related to tracking of DataSet deletions.
        if (segment.way.getNodesCount() <= segment.lowerIndex + 1) {
            Logging.warn("Not drawing AlignWays highlighting segment: underlying nodes disappeared");
            return;
        }

        highlightSegment(segmentColor, g, mv);
    }

    protected void highlightSegment(Color c, Graphics2D g, MapView mv) {
        g.setColor(c);
        g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawSegment(g, mv);
    }

    protected void drawSegment(Graphics2D g, MapView mv) {
        try {
            Node n1 = segment.getFirstNode();
            Node n2 = segment.getSecondNode();

            g.draw(new Line2D.Double(mv.getPoint(n1), mv.getPoint(n2)));
        } catch (IndexOutOfBoundsException e) {
            Logging.error(e);
        }
    }

    protected boolean containsPrimitive(OsmPrimitive primitive) {
        if (segment == null)
            return false;

        return (primitive instanceof Way && segment.way.equals(primitive)) ||
                (primitive instanceof Node && segmentEndPoints.contains(primitive));

    }

     @Override
     public int hashCode() {
        if (segment == null) {
            return System.identityHashCode(this);
        }

        // hashCode and equals should be consistent during the lifetime of this segment,
        // otherwise the temporary mapview overlay will not be properly removed on destroy()
        return segment.hashCode();
     }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlignWaysSegment)) return false;
        AlignWaysSegment that = (AlignWaysSegment) o;
        return Objects.equals(segment, that.segment);
    }

}
