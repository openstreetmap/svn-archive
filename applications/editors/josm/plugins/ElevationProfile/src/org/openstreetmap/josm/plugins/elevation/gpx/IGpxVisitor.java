// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gpx;

import org.openstreetmap.josm.data.gpx.GpxRoute;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * Interface for all GPX visitors.
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public interface IGpxVisitor extends IGpxWaypointVisitor {
    /**
     * Starts a GPX way point collection.
     */
    void beginWayPoints();

    /**
     * Ends a GPX way point collection.
     */
    void endWayPoints();

    /**
     * Starts a GPX track.
     */
    void beginTrack(GpxTrack track);

    /**
     * Ends a GPX track.
     */
    void endTrack(GpxTrack track);

    /**
     * Starts a GPX route.
     */
    void beginRoute(GpxRoute track);

    /**
     * Ends a GPX route.
     */
    void endRoute(GpxRoute track);


    /**
     * Starts a segment within a GPX track.
     */
    void beginTrackSegment(GpxTrack track, GpxTrackSegment segment);

    /**
     * Ends a segment within a GPX track.
     */
    void endTrackSegment(GpxTrack track, GpxTrackSegment segment);

    /**
     * Visits a way point within a GPX route.
     * @param route The route containing the way point.
     * @param wp The way point to visit.
     */
    @Override
    void visitWayPoint(WayPoint wp);

    /**
     * Visits a way point within a GPX track.
     *
     * @param wp The way point to visit.
     * @param track the track containing the way point.
     * @param segment the track segment
     */
    void visitTrackPoint(WayPoint wp, GpxTrack track, GpxTrackSegment segment);

    /**
     * Visits a way point within a GPX route.
     * @param route the route containing the way point.
     * @param wp the way point to visit.
     */
    void visitRoutePoint(WayPoint wp, GpxRoute route);
}
