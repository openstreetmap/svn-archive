// License: Public Domain. For details, see LICENSE file.
package livegps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.IGpxTrack;
import org.openstreetmap.josm.data.gpx.IGpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WithAttributes;

public class SingleSegmentGpxTrack extends WithAttributes implements IGpxTrack {

    private final IGpxTrackSegment trackSegment;

    public SingleSegmentGpxTrack(IGpxTrackSegment trackSegment, Map<String, Object> attributes) {
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
    public Collection<IGpxTrackSegment> getSegments() {
        return Collections.singleton(trackSegment);
    }

    @Override
    public double length() {
        return trackSegment.length();
    }
}
