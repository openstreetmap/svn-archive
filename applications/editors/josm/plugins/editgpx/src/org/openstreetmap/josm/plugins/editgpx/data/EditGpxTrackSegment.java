package org.openstreetmap.josm.plugins.editgpx.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;

public class EditGpxTrackSegment {

    private final List<EditGpxWayPoint> wayPoints = new ArrayList<EditGpxWayPoint>();
    private boolean deleted;

    public EditGpxTrackSegment(GpxTrackSegment segment) {
        for (WayPoint wayPoint: segment.getWayPoints()) {
            wayPoints.add(new EditGpxWayPoint(wayPoint));
        }
    }

    public List<EditGpxWayPoint> getWayPoints() {
        return wayPoints;
    }

    public List<WayPoint> getNonDeletedWaypoints() {
        List<WayPoint> result = new ArrayList<WayPoint>();

        for (EditGpxWayPoint wp: wayPoints) {
            if (!wp.isDeleted()) {
                result.add(wp.createWayPoint());
            }
        }

        return result;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * time of the oldest waypoint in the set of non-deleted waypoints
     * in this segment (in seconds since Epoch)
     */
    public double minNonDeletedTime() {
        return Collections.min(getNonDeletedWaypoints()).time;
    }

}
