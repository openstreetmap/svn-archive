/*
 *      GpxFilter.java
 *
 *      Copyright 2011 Hind <foxhind@gmail.com>
 *
 */

package CommandLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.BBox;

public class GpxFilter {
    private BBox bbox;
    private final GpxData data;

    public GpxFilter() {
        bbox = new BBox(0.0, 0.0, 0.0, 0.0);
        data = new GpxData();
    }

    public void initBboxFilter(BBox bbox) {
        this.bbox = bbox;
    }

    public void addGpxData(GpxData data) {
        Collection<Collection<WayPoint>> currentTrack;
        Collection<WayPoint> currentSegment;
        for (GpxTrack track : data.tracks) {
            currentTrack = new ArrayList<>();
            for (GpxTrackSegment segment : track.getSegments()) {
                currentSegment = new ArrayList<>();
                for (WayPoint wp : segment.getWayPoints()) {
                    if ( bbox.bounds(wp.getCoor()) ) {
                        currentSegment.add(wp);
                    } else {
                        if (currentSegment.size() > 1) {
                            currentTrack.add(currentSegment);
                            currentSegment = new ArrayList<>();
                        }
                    }
                }
                if (currentSegment.size() > 1) {
                    currentTrack.add(currentSegment);
                    currentSegment = new ArrayList<>();
                }
            }
            this.data.tracks.add( new ImmutableGpxTrack( currentTrack, Collections.<String, Object>emptyMap()) );
        }
    }

    public GpxData getGpxData() {
        return data;
    }
}
