package org.openstreetmap.josm.plugins.dataimport.io;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WithAttributes;

public class SingleSegmentGpxTrack extends WithAttributes implements GpxTrack {

    private final GpxTrackSegment trackSegment;

    public SingleSegmentGpxTrack(GpxTrackSegment trackSegment, Map<String, Object> attributes) {
        this.attr = Collections.unmodifiableMap(attributes);
        this.trackSegment = trackSegment;
    }


    public Map<String, Object> getAttributes() {
        return attr;
    }

    public Bounds getBounds() {
        return trackSegment.getBounds();
    }

    public Collection<GpxTrackSegment> getSegments() {
        return Collections.singleton(trackSegment);
    }

    public double length() {
        return trackSegment.length();
    }

    @Override
    public int getUpdateCount() {
        return trackSegment.getUpdateCount();
    }

}
