package org.openstreetmap.josm.plugins.editgpx.data;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxRoute;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;

public class EditGpxData {

    private final List<EditGpxTrack> tracks = new ArrayList<EditGpxTrack>();
    // Only copy of routes and waypoints to preserve all info when converting back to gpx track
    private final List<GpxRoute> routes = new ArrayList<GpxRoute>();
    private final List<WayPoint> waypoints = new ArrayList<WayPoint>();

    public void load(GpxData data) {
        for (GpxTrack track: data.tracks) {
            tracks.add(new EditGpxTrack(track));
        }
        routes.clear();
        routes.addAll(data.routes);
        waypoints.clear();
        waypoints.addAll(data.waypoints);
    }

    public boolean isEmpty() {
        for (EditGpxTrack track: tracks) {
            for (EditGpxTrackSegment segment: track.getSegments()) {
                if (!segment.getWayPoints().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<EditGpxTrack> getTracks() {
        return tracks;
    }

    public GpxData createGpxData(boolean anonTime) {
        GpxData result = new GpxData();

        for (EditGpxTrack track: tracks) {
            if (!track.isDeleted()) {
                GpxTrack newTrack = track.createGpxTrack(anonTime);
                if (!newTrack.getSegments().isEmpty()) {
                    result.tracks.add(newTrack);
                }
            }
        }

        result.routes.addAll(routes);
        result.waypoints.addAll(waypoints);
        return result;
    }

}
