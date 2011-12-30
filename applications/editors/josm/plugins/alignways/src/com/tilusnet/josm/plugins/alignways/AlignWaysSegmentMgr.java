package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
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

    public static AlignWaysSegmentMgr getInstance(MapView mapView) {
        if (singleton == null) {
            synchronized (AlignWaysSegmentMgr.class) {
                if (singleton == null) {
                    singleton = new AlignWaysSegmentMgr(mapView);
                }
            }
        }
        return singleton;
    }

    /**
     * @param clickedPoint
     *            Point nearby where user probably clicked
     * @return true, if alignee changed, false otherwise
     */
    public boolean algnUpdate(Point clickedPoint) {

        if (algnSeg != null) {
            // Check first if there is a pivot point nearby that needs selection
            if (algnSeg.updatePivot(clickedPoint))
                // Updated pivot, alignee reference unchanged
                return false;
        }

        // Previous attempt of pivot update unsuccessful, check alignee update
        AlignWaysAlgnSegment tmpAlgnSeg = new AlignWaysAlgnSegment(mv,
                clickedPoint);
        if (tmpAlgnSeg.getSegment() == null)
            return false;
        else {
            // Found a segment
            // It may happen that the new segment is identical with the already
            // selected reference:
            if ((refSeg != null) && (tmpAlgnSeg.equals(refSeg))) {
                // This action is then ignored (we won't clear the reference
                // segment)
                JOptionPane.showMessageDialog(Main.parent,
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

    }

    /**
     * @param clickedPoint
     *            Point nearby where user probably clicked
     * @return true, if reference changed, false otherwise
     */
    public boolean refUpdate(Point clickedPoint) {

        AlignWaysRefSegment tmpRefSeg = new AlignWaysRefSegment(mv,
                clickedPoint);
        // TODO Have to check what happens when refSeg wasn't null previously
        if (tmpRefSeg.getSegment() == null)
            return false;
        else {
            // Found a segment
            // It may happen that the new segment is identical with the already
            // selected alignee:
            if ((algnSeg != null) && (tmpRefSeg.equals(algnSeg))) {
                // This action is then ignored (we won't clear the alignee
                // segment)
                JOptionPane.showMessageDialog(Main.parent,
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

}
