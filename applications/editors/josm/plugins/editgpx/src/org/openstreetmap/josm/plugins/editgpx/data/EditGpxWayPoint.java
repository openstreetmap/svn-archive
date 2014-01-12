package org.openstreetmap.josm.plugins.editgpx.data;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;

public class EditGpxWayPoint implements Comparable<EditGpxWayPoint> {
    private final double time;
    private final CachedLatLon coor;
    private boolean deleted;
    private Map<String, Object> attributes;

    public EditGpxWayPoint(WayPoint wayPoint) {
        this.time = wayPoint.time;
        this.coor = new CachedLatLon(wayPoint.getCoor());
        this.attributes = new HashMap<String, Object>(wayPoint.attr);
    }

    public WayPoint createWayPoint() {
        WayPoint result = new WayPoint(getCoor());
        result.time = time;
        result.attr = attributes;
        return result;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * returns this waypoint's time in seconds since Epoch
     */
    public double getTime() {
        return time;
    }

    public CachedLatLon getCoor() {
        return coor;
    }

    public int compareTo(EditGpxWayPoint o) {
        return Double.compare(getTime(), o.getTime());
    }
}
