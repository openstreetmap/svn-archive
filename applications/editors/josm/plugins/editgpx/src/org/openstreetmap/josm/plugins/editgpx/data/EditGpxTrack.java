package org.openstreetmap.josm.plugins.editgpx.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;

public class EditGpxTrack {

    private final List<EditGpxTrackSegment> segments = new ArrayList<EditGpxTrackSegment>();
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private boolean isDeleted;

    public EditGpxTrack(GpxTrack track) {
        attributes.putAll(track.getAttributes());
        for (GpxTrackSegment segment: track.getSegments()) {
            segments.add(new EditGpxTrackSegment(segment));
        }
    }

    public List<EditGpxTrackSegment> getSegments() {
        return segments;
    }
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public GpxTrack createGpxTrack(boolean anonTime) {

        Collection<Collection<WayPoint>> wayPoints = new ArrayList<Collection<WayPoint>>();

        for (EditGpxTrackSegment segment: segments) {
            if (!segment.isDeleted()) {
                List<WayPoint> points = segment.getNonDeletedWaypoints();
                if (!points.isEmpty()) {
                    if (anonTime) {
                        // convert to anonymous time
                        for (WayPoint w : points) {
                            w.attr.put("time", "1970-01-01T00:00:00.000Z");
                            w.setTime();
                            if (w.attr.containsKey("name")) {
                                w.attr.put("name", "anon"); //time information can also be in "name" field. so delete time information
                            }
                        }
                    }
                    wayPoints.add(points);
                }
            }
        }
        if (anonTime) {
            if (attributes.containsKey("name")) {
                attributes.put("name", "anon");//time information can also be in "name" field. so delete time information
            }
        }

        return new ImmutableGpxTrack(wayPoints, attributes);
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}
