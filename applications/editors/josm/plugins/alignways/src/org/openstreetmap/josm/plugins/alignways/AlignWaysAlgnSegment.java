package org.openstreetmap.josm.plugins.alignways;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.EnumMap;
import java.util.Map;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;

/**
 * @author tilusnet <tilusnet@gmail.com> The segment to be aligned to the
 *         reference segment. Actions it can do: - remember its selected pivot
 *         point - keeps its potential pivot point list up to date - rotate
 *         itself - paint itself and its selected pivot point
 *
 */
public class AlignWaysAlgnSegment extends AlignWaysSegment {

    private enum PivotLocations {
        NONE, NODE1, NODE2, CENTRE
    };

    private PivotLocations currPivot;
    Map<PivotLocations, EastNorth> pivotList = new EnumMap<PivotLocations, EastNorth>(
            PivotLocations.class);
    private final Color pivotColor = Color.YELLOW;
    private final Color crossColor = pivotColor;

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
     */
    private EastNorth getPivotCoord(PivotLocations pp) {
        switch (pp) {
        case NONE:
            return null;
        case NODE1:
            return segment.way.getNode(segment.lowerIndex).getEastNorth();
        case NODE2:
            return segment.way.getNode(segment.lowerIndex + 1).getEastNorth();
        case CENTRE:
            return getPivotCoord(PivotLocations.NODE1).getCenter(
                    getPivotCoord(PivotLocations.NODE2));
        default:
            // Should never happen
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openstreetmap.josm.plugins.alignways.AlignWaysRefSegment#paint(java
     * .awt.Graphics2D, org.openstreetmap.josm.gui.MapView,
     * org.openstreetmap.josm.data.Bounds)
     */
    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        // Note: segment should never be null here
        super.paint(g, mv, bbox);

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
        g.draw(crossV);
        g.draw(crossH);

    }

}
