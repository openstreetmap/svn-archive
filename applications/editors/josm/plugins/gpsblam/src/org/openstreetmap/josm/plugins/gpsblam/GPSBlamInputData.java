// License: GPL. Copyright (C) 2012 Russell Edwards
package org.openstreetmap.josm.plugins.gpsblam;

import java.awt.Point;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;

import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;

class GPSBlamInputData extends LinkedList<CachedLatLon> {
    private final Collection<Calendar> datesSeen = new HashSet<>();

    public int getNDays() {
        return datesSeen.size();
    }

    // select a set of GPX points and count how many tracks they have come from,
    // within given radius of line between given points
    GPSBlamInputData(Point p1, Point p2, int radius) {
        Collection<Layer> layers = MainApplication.getLayerManager().getLayers();
        Projection projection = ProjectionRegistry.getProjection();
        for (Layer l : layers) {
            if (l.isVisible() && l instanceof GpxLayer) {
                for (GpxTrack track : ((GpxLayer) l).data.tracks) {
                    for (GpxTrackSegment segment: track.getSegments()) {
                        for (WayPoint wayPoint : segment.getWayPoints()) {
                            if (p2.equals(p1)) {
                                // circular selection
                                CachedLatLon cll = new CachedLatLon(wayPoint.getCoor());
                                Point p = MainApplication.getMap().mapView.getPoint(cll.getEastNorth(projection));
                                if (p.distance(p1) < radius) {
                                    this.add(cll, wayPoint);
                                }
                            } else {
                                // selection based on distance from line from p1 to p2
                                // hack to work for zero length
                                double length = Math.sqrt((p2.x-p1.x)*(p2.x-p1.x)+(p2.y-p1.y)*(p2.y-p1.y))+1e-5;
                                // unit direction vector from p1.x,p1.y to p2.x, p2.y
                                double dirX = (p2.x-p1.x)/length, dirY = (p2.y-p1.y)/length;
                                // unit vector 90deg CW from direction vector
                                double perpdirX = dirY, perpdirY = -dirX;

                                CachedLatLon cll = new CachedLatLon(wayPoint.getCoor());
                                Point p = MainApplication.getMap().mapView.getPoint(cll.getEastNorth(projection));
                                double pX = p.x-p1.x, pY = p.y-p1.y; // vector from point clicked to waypoint
                                double pPar = pX*dirX + pY*dirY; // parallel component
                                double pPerp = pX*perpdirX + pY*perpdirY; // perpendicular component
                                double pardist = 0.0;

                                if (pPar < 0)
                                    pardist = -pPar;
                                else if (pPar > length)
                                    pardist = pPar-length;

                                double distsq = pardist*pardist + pPerp*pPerp;

                                if (distsq < radius*radius) {
                                    this.add(cll, wayPoint);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void add(CachedLatLon cll, WayPoint wayPoint) {
        this.add(cll);
        Calendar day = new GregorianCalendar();
        day.setTimeInMillis(wayPoint.getTimeInMillis());
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        datesSeen.add(day);
    }
}