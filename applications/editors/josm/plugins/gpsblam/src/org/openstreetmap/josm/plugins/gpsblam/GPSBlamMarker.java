// License: GPL. Copyright (C) 2012 Russell Edwards
package org.openstreetmap.josm.plugins.gpsblam;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MapView;

class GPSBlamMarker {
    private final CachedLatLon mean;
    private final CachedLatLon hair1Coord1, hair1Coord2, hair2Coord1, hair2Coord2;
    private final CachedLatLon ellipseCoord1, ellipseCoord2, ellipseCoord3; // 1=TL 2=TR 3=BL, where main axis = +R, minor +U
    private static final double FAC = 2.45; // 2.45 gives 95% CI for 2D

    /**
     * Construct a blammarker by analysis of selected GPS points
     * @param inputData input data
     */
    GPSBlamMarker(GPSBlamInputData inputData) {
        Projection projection = ProjectionRegistry.getProjection();
        // get mean east, north
        double meanEast=0.0, meanNorth=0.0;
        for (CachedLatLon cll : inputData) {
            EastNorth en = cll.getEastNorth(projection);
            meanEast += en.east();
            meanNorth += en.north();
        }
        double n = inputData.size();
        meanEast /= n;
        meanNorth /= n;

        // get covariance matrix
        double ca=0.0, cb=0.0, cc=0.0, cd=0.0;
        double deast, dnorth;
        for (CachedLatLon cll : inputData) {
            EastNorth en = cll.getEastNorth(projection);
            deast = en.east()-meanEast;
            dnorth = en.north()-meanNorth;
            ca += deast*deast;
            cb += deast*dnorth;
            cd += dnorth*dnorth;
        }
        cc = cb;
        ca /= n;
        cb /= n;
        cc /= n;
        cd /= n;

        // direction and spread analysis
        double t = ca+cd, d = ca*cd-cb*cc; // trace, determinant
        double variance1 = 0.5*t + Math.sqrt(0.25*t*t-d); // Eigenvalue 1
        double variance2 = 0.5*t - Math.sqrt(0.25*t*t-d); // Eigenvalue 2
        double evec1East = variance1-cd, evec1North = cc; // eigenvec1
        double evec2East = variance2-cd, evec2North = cc; // eigenvec2

        double evec1Fac = Math.sqrt(variance1)/Math.sqrt(evec1East*evec1East+evec1North*evec1North);
        double evec2Fac = Math.sqrt(variance2)/Math.sqrt(evec2East*evec2East+evec2North*evec2North);
        double sigma1East = evec1East * evec1Fac, sigma1North = evec1North * evec1Fac;
        double sigma2East = evec2East * evec2Fac, sigma2North = evec2North * evec2Fac;

        // save latlon coords of the mean and the ends of the crosshairs
        Projection proj = ProjectionRegistry.getProjection();
        mean = new CachedLatLon(proj.eastNorth2latlon(new EastNorth(meanEast, meanNorth)));
        hair1Coord1 = new CachedLatLon(proj.eastNorth2latlon(
                   new EastNorth(meanEast-sigma1East*FAC, meanNorth-sigma1North*FAC)));
        hair1Coord2 = new CachedLatLon(proj.eastNorth2latlon(
                   new EastNorth(meanEast+sigma1East*FAC, meanNorth+sigma1North*FAC)));
        hair2Coord1 = new CachedLatLon(proj.eastNorth2latlon(
                   new EastNorth(meanEast-sigma2East*FAC, meanNorth-sigma2North*FAC)));
        hair2Coord2 = new CachedLatLon(proj.eastNorth2latlon(
                   new EastNorth(meanEast+sigma2East*FAC, meanNorth+sigma2North*FAC)));
        double efac = FAC/Math.sqrt(inputData.getNDays());
        // TopLeft, TopRight, BottomLeft in frame where sigma1=R sigma2=Top
        ellipseCoord1 = new CachedLatLon(proj.eastNorth2latlon(
                   new EastNorth(meanEast+(-sigma1East+sigma2East)*efac, meanNorth+(-sigma1North+sigma2North)*efac))); //
        ellipseCoord2 = new CachedLatLon(proj.eastNorth2latlon(
                   new EastNorth(meanEast+(sigma1East+sigma2East)*efac, meanNorth+(sigma1North+sigma2North)*efac))); //
        ellipseCoord3 = new CachedLatLon(proj.eastNorth2latlon(
                   new EastNorth(meanEast+(-sigma1East-sigma2East)*efac, meanNorth+(-sigma1North-sigma2North)*efac))); //
    }

    void paint(Graphics2D g, MapView mv) {
        Projection projection = ProjectionRegistry.getProjection();
        g.setColor(Color.GREEN);
        g.setPaintMode();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2.0f));
        Point hair1Point1 = mv.getPoint(hair1Coord1.getEastNorth(projection));
        Point hair1Point2 = mv.getPoint(hair1Coord2.getEastNorth(projection));
        Point hair2Point1 = mv.getPoint(hair2Coord1.getEastNorth(projection));
        Point hair2Point2 = mv.getPoint(hair2Coord2.getEastNorth(projection));
        g.drawLine(hair1Point1.x, hair1Point1.y, hair1Point2.x, hair1Point2.y);
        g.drawLine(hair2Point1.x, hair2Point1.y, hair2Point2.x, hair2Point2.y);

        Point2D meanPoint = mv.getPoint2D(mean.getEastNorth(projection));
        Point2D ellipsePoint1 = mv.getPoint2D(ellipseCoord1.getEastNorth(projection));
        Point2D ellipsePoint2 = mv.getPoint2D(ellipseCoord2.getEastNorth(projection));
        Point2D ellipsePoint3 = mv.getPoint2D(ellipseCoord3.getEastNorth(projection));
        double majorAxis = ellipsePoint2.distance(ellipsePoint1);
        double minorAxis = ellipsePoint3.distance(ellipsePoint1);
        double angle = -Math.atan2(-(ellipsePoint2.getY()-ellipsePoint1.getY()), ellipsePoint2.getX()-ellipsePoint1.getX());
        Shape e = new Ellipse2D.Double(meanPoint.getX()-majorAxis*0.5, meanPoint.getY()-minorAxis*0.5,
                                        majorAxis, minorAxis);
        g.rotate(angle, meanPoint.getX(), meanPoint.getY());
        g.draw(e);
        g.rotate(-angle, meanPoint.getX(), meanPoint.getY());
    }
}
