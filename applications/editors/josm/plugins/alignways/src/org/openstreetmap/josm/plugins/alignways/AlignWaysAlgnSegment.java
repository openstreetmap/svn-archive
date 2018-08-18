// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.alignways;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.tools.Logging;

/**
 * @author tilusnet &lt;tilusnet@gmail.com&gt;
 *
 * The segment to be aligned to the reference segment. Actions it can do:
 *         - remember its selected pivot point
 *         - keeps its potential pivot point list up to date
 *         - rotate itself
 *         - paint itself and its selected pivot point
 *
 */
public class AlignWaysAlgnSegment extends AlignWaysSegment {

    private enum PivotLocations {
        NONE, NODE1, NODE2, CENTRE
    }

    private PivotLocations currPivot;
    Map<PivotLocations, EastNorth> pivotList = new EnumMap<>(
            PivotLocations.class);
    private final Color pivotColor = Color.YELLOW;
    private final Color crossColor = pivotColor;
    private final Map<Node, ArrayList<WaySegment>> adjWaySegs = new HashMap<>();

    public AlignWaysAlgnSegment(MapView mapview, Point p)
            throws IllegalArgumentException {
        super(mapview, p);
        setSegment(getNearestWaySegment(p));
        segmentColor = Color.ORANGE;
    }

    /**
     * Sets segment and initialises its pivot list and activates the centre
     * rotation pivot.
     */
    @Override
    public void setSegment(WaySegment segment) {
        super.setSegment(segment);
        setPivots();
    }

    @Override
    void setSegmentEndpoints(WaySegment segment) {
        super.setSegmentEndpoints(segment);

        // Update the list of adjacent waysegments to the endpoints
        for (Node nA : getSegmentEndPoints()) {
            adjWaySegs.put(nA, new ArrayList<>(determineAdjacentWaysegments(nA)));
        }
    }

    /**
     * Useful when segments moves (or e.g. rotates) on the map. Updates the end
     * segment points and the pivot coordinates without changing the current
     * pivot.
     */
    public void updatePivotsEndpoints() {
        setPivots(currPivot);
        setSegmentEndpoints(segment);
    }

    /**
     * Updates the segment's pivot list and sets the rotation pivot to centre.
     * @param pivotRef pivot reference
     */
    private void setPivots(PivotLocations pivotRef) {
        if (segment != null) {
            for (PivotLocations pl : PivotLocations.values()) {
                pivotList.put(pl, getPivotCoord(pl));
            }
            setPivotReference(pivotRef);
        } else {
            setPivotReference(PivotLocations.NONE);
        }
    }

    private void setPivots() {
        setPivots(PivotLocations.CENTRE);
    }

    private void setPivotReference(PivotLocations pp) {
        currPivot = pp;
    }

    /**
     * Returns the EastNorth of the specified pivot point pp. It always returns
     * up-to-date data from dataset. Assumes segment is not null.
     *
     * @param pp
     *            The pivot location
     * @return east/north coordinates of pivot location
     */
    private EastNorth getPivotCoord(PivotLocations pp) {
        try {
            EastNorth n1;
            EastNorth n2;
            switch (pp) {
            case NODE1:
                return segment.getFirstNode().getEastNorth();
            case NODE2:
                return segment.getSecondNode().getEastNorth();
            case CENTRE:
                n1 = getPivotCoord(PivotLocations.NODE1);
                n2 = getPivotCoord(PivotLocations.NODE2);
                return n1 != null && n2 != null ? n1.getCenter(n2) : null;
            case NONE:
            default:
                return null;
            }
        } catch (IndexOutOfBoundsException e) {
            Logging.error(e);
            return null;
        }
    }

    /**
     * @return The EastNorth of the currently selected pivot.
     */
    public EastNorth getCurrPivotCoord() {
        if (segment != null)
            return getPivotCoord(currPivot);
        return null;
    }

    /**
     * @param clickedPoint
     *            Pivot may be updated in the vicinity of this point
     * @return true if a pivot is within reach on the segment, false otherwise
     */
    public boolean updatePivot(Point clickedPoint) {
        // tHQ Done.
        PivotLocations tmpPivot = findNearbyPivot(clickedPoint);
        if (tmpPivot != PivotLocations.NONE) {
            setPivotReference(tmpPivot);
            return true;
        } else
            return false;
    }

    private PivotLocations findNearbyPivot(Point clickedPoint) {
        PivotLocations nearest = PivotLocations.NONE;
        int snapDistance = NavigatableComponent.PROP_SNAP_DISTANCE.get();

        // If no alignee selected yet, there's no point to carry on
        if (segment == null)
            return PivotLocations.NONE;

        for (PivotLocations pl : PivotLocations.values()) {
            if (pl.equals(PivotLocations.NONE)) {
                continue;
            }
            if (mapview.getPoint(pivotList.get(pl)).distance(clickedPoint) <= snapDistance) {
                nearest = pl;
                break;
            }
        }
        return nearest;
    }

    /**
     * Given a Node (usually an endpoint), it will return a collection of way segments that are adjacently
     * connected to it. The current alignee waysegment is not added to the collection.
     *
     * @param node The Node (endpoint) to analyse.
     * @return The collection of the adjacent waysegments.
     */
    private Collection<WaySegment> determineAdjacentWaysegments(Node node) {
        Collection<WaySegment> wsSet = new HashSet<>();
        final double radius = 10.0;
        final int stepsOnCircle = 24;
        final double incrementOnCircle = 2 * Math.PI / stepsOnCircle;

        Point p = MainApplication.getMap().mapView.getPoint(node);
        for (int i = 0; i < stepsOnCircle; i++) {
            double ang = i * incrementOnCircle;
            double x = p.getX() + (Math.cos(ang) * radius);
            double y = p.getY() + (Math.sin(ang) * radius);
            Point pnew = new Point();
            pnew.setLocation(x, y);
            WaySegment ws = MainApplication.getMap().mapView.getNearestWaySegment(pnew, OsmPrimitive::isUsable);
            if (ws != null && !ws.equals(this.segment) &&
                    (ws.getFirstNode().equals(node) || ws.getSecondNode().equals(node))) {
                // We won't want to add a:
                // - 'no match' (=null)
                // - segment that is not connected to the alignee endpoint
                wsSet.add(ws);
            }
        }

        return wsSet;
    }

    /**
     * Returns the collection of adjacent way segments to Node node.
     * The node is normally a valid endpoint of the segment.
     * If it isn't, null may be returned.
     *
     * @param node The (endpoint) node.
     * @return Collection of the adjacent way segments.
     */
    public ArrayList<WaySegment> getAdjacentWaySegments(Node node) {
        return adjWaySegs.get(node);
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        super.paint(g, mv, bbox);

        // Note: segment should never be null here, and its nodes should never be missing.
        // If they are, it's a bug, possibly related to tracking of DataSet deletions.
        if (segment.way.getNodesCount() <= segment.lowerIndex + 1) {
            Logging.warn("Not drawing AlignWays pivot points: underlying nodes disappeared");
            return;
        }

        // Ensure consistency
        updatePivotsEndpoints();

        // Highlight potential pivot points
        for (PivotLocations pl : PivotLocations.values()) {
            if (pl != PivotLocations.NONE) {
                highlightCross(g, mv, pivotList.get(pl));
            }
        }

        // Highlight active pivot
        highlightPivot(g, mv, getPivotCoord(currPivot));
    }

    private void highlightPivot(Graphics2D g, MapView mv, EastNorth pivot) {
        g.setColor(pivotColor);
        g.setStroke(new BasicStroke());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape pvCentrePoint = new Ellipse2D.Double(
                mv.getPoint(pivot).getX() - 5.0f,
                mv.getPoint(pivot).getY() - 5.0f, 10.0f, 10.0f);
        g.fill(pvCentrePoint);
        Shape pvPoint = new Ellipse2D.Double(mv.getPoint(pivot).getX() - 8.0f,
                mv.getPoint(pivot).getY() - 8.0f, 16.0f, 16.0f);

        g.draw(pvCentrePoint);
        g.draw(pvPoint);
    }

    private void highlightCross(Graphics2D g, MapView mv, EastNorth en) {

        double crossX = mv.getPoint(en).getX();
        double crossY = mv.getPoint(en).getY();
        double crossSize = 10.0;

        Line2D crossV = new Line2D.Double(crossX, crossY - crossSize, crossX,
                crossY + crossSize);
        Line2D crossH = new Line2D.Double(crossX - crossSize, crossY, crossX
                + crossSize, crossY);

        g.setColor(crossColor);
        g.setStroke(new BasicStroke());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.draw(crossV);
        g.draw(crossH);

    }

}
