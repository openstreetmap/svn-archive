package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;

/**
 * Should be a singleton class. The active alignee and reference objects
 * manager. Can request alignee, reference segment updates and pivot updates on
 * these.
 * 
 * @author tilusnet <tilusnet@gmail.com>
 * 
 */
public class AlignWaysSegmentMgr {

    private volatile static AlignWaysSegmentMgr singleton;
    private AlignWaysRefSegment refSeg = null;
    private AlignWaysAlgnSegment algnSeg = null;
    private final MapView mv;

    private AlignWaysSegmentMgr(MapView mapView) {
        mv = mapView;
    }

    /**
     * Get or creates the segment manager instance belonging to a given MapView.
     */
    public static AlignWaysSegmentMgr getInstance(MapView mapView) {
        synchronized (AlignWaysSegmentMgr.class) {
            if (singleton == null || !singleton.getMapView().equals(mapView)) {
                singleton = new AlignWaysSegmentMgr(mapView);
            }
        }
        return singleton;
    }

    /**
     * @return The MapView this segment manager belongs to.
     */
    private MapView getMapView() {
        return mv;
    }

    /**
     * @param clickedPoint
     *            Point nearby where user probably clicked
     * @return true, if alignee changed, false otherwise
     */
    public boolean algnUpdate(Point clickedPoint) {

        // Check first if there is a pivot point nearby that needs selection
        if (algnSeg != null && algnSeg.updatePivot(clickedPoint)) {
            // Updated pivot, alignee reference unchanged
            return false;
        }

        // Previous attempt of pivot update unsuccessful, check alignee update
        AlignWaysAlgnSegment tmpAlgnSeg = new AlignWaysAlgnSegment(mv, clickedPoint);
        if (tmpAlgnSeg.getSegment() == null)
            return false;

        // Found a segment - it may happen that the new segment is identical with the already
        // selected asignee or reference segment. This action is then ignored.
        if (algnSeg != null && tmpAlgnSeg.equals(algnSeg)) {
            return false;
        }
        else if (refSeg != null && tmpAlgnSeg.equals(refSeg)) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Segment to be aligned cannot be the same with the reference segment.\n" +
                    "Please choose a different segment to be aligned."),
                    tr("AlignWayS message"), JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // This will be a new alignee, old alignee (if any) will be lost:
        if (algnSeg != null) {
            algnSeg.destroy();
        }

        // Update alignee
        algnSeg = tmpAlgnSeg;
        return true;
    }

    /**
     * @param clickedPoint
     *            Point nearby where user probably clicked
     * @return true, if reference changed, false otherwise
     */
    public boolean refUpdate(Point clickedPoint) {

        AlignWaysRefSegment tmpRefSeg = new AlignWaysRefSegment(mv, clickedPoint);
        if (tmpRefSeg.getSegment() == null)
            return false;

        // Found a segment - it may happen that the new segment is identical with the already
        // selected asignee or reference segment. This action is then ignored.
        if (refSeg != null && refSeg.equals(tmpRefSeg)) {
            return false;
        }
        else if (algnSeg != null && tmpRefSeg.equals(algnSeg)) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Reference segment cannot be the same with the segment to be aligned.\n" +
                    "Please choose a different reference segment."),
                    tr("AlignWayS message"), JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // This will be a new reference, old reference (if any) will be lost:
        if (refSeg != null) {
            refSeg.destroy();
        }

        // Update reference
        refSeg = tmpRefSeg;
        return true;
    }

    /**
     * @return Collection of the nodes that belong to the selected alignee.
     */
    public Collection<Node> getSelectedNodes() {
        if (algnSeg != null)
            return algnSeg.getSegmentEndPoints();
        return null;
    }

    /**
     * Performs "clean-up" on the initialised segments
     */
    public void cleanupWays() {
        if (algnSeg != null) {
            algnSeg.destroy();
            algnSeg = null;
        }
        if (refSeg != null) {
            refSeg.destroy();
            refSeg = null;
        }
    }

    public AlignWaysAlgnSegment getAlgnSeg() {
        return algnSeg;
    }

    /**
     * @return the refSeg
     */
    public AlignWaysRefSegment getRefSeg() {
        return refSeg;
    }


    /**
     * Handles the event that OSM primitives were removed and checks whether
     * this invalidates the currently selected alignee or reference segment.
     * @return Whether any of the selected segments were invalidated.
     * @param primitives List of primitives that were removed.
     */
    public boolean primitivesRemoved(List<? extends OsmPrimitive> primitives) {
        boolean changed = false;
        for (OsmPrimitive p : primitives) {
            if (algnSeg != null && algnSeg.containsPrimitive(p)) {
                algnSeg.destroy();
                algnSeg = null;
                changed = true;
            }
            if (refSeg != null && refSeg.containsPrimitive(p)) {
                refSeg.destroy();
                refSeg = null;
                changed = true;
            }
        }
        return changed;
    }
}
