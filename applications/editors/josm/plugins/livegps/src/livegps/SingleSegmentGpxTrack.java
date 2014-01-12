package livegps;

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


    @Override
    public Map<String, Object> getAttributes() {
        return attr;
    }

    @Override
    public Bounds getBounds() {
        return trackSegment.getBounds();
    }

    @Override
    public Collection<GpxTrackSegment> getSegments() {
        return Collections.singleton(trackSegment);
    }

    @Override
    public double length() {
        return trackSegment.length();
    }

    @Override
    public int getUpdateCount() {
        return trackSegment.getUpdateCount();
    }

}
