package org.openstreetmap.josm.plugins.editgpx.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

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

    public GpxTrack createGpxTrack(boolean anonTime, double minTime) {

        Collection<Collection<WayPoint>> wayPoints = new ArrayList<Collection<WayPoint>>();

        final DateFormat iso8601 =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        final TimeZone utc = TimeZone.getTimeZone("UTC");
        iso8601.setTimeZone(utc);

        for (EditGpxTrackSegment segment: segments) {
            if (!segment.isDeleted()) {
                List<WayPoint> points = segment.getNonDeletedWaypoints();
                if (!points.isEmpty()) {
                    if (anonTime) {
                        // convert to anonymous time
                        for (WayPoint w : points) {
                            double t = w.time - minTime;
                            w.attr.put("time", iso8601.format(
                                    new Date((long)(t * 1000))));
                            w.setTime();
                            assert w.time == t;
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

    /**
     * time of the oldest waypoint in the set of non-deleted waypoints
     * in this track (in seconds since Epoch)
     */
    public double minNonDeletedTime() {
        boolean foundOne = false;
        double minTime = 0.0;

        for (EditGpxTrackSegment segment: segments) {
            if (!segment.isDeleted()) {
                try {
                    double t = segment.minNonDeletedTime();
                    if ((!foundOne) || (t < minTime)) {
                        minTime = t;
                    }
                    foundOne = true;
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
        }

        if (!foundOne) {
            throw new NoSuchElementException();
        }
        return minTime;
    }
}
