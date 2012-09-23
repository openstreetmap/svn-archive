/** GPSBlam JOSM Plugin
 * Copyright (C) 2012 Russell Edwards
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.gpsblam;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;

public class GPSBlamMarker {
	private CachedLatLon mean;
	private CachedLatLon hair1Coord1, hair1Coord2, hair2Coord1, hair2Coord2;
	private CachedLatLon ellipseCoord1, ellipseCoord2, ellipseCoord3; // 1=TL 2=TR 3=BL, where main axis = +R, minor +U 
	private int ndays;
	static final double fac = 2.45; // 2.45 gives 95% CI for 2D
	
	// construct a blammarker by analysis of selected GPS points
	public GPSBlamMarker(GPSBlamInputData inputData)
	    {
	        // get mean east, north
	        double mean_east=0.0, mean_north=0.0;
	        for (CachedLatLon cll : inputData)
	        {
	        	EastNorth en = cll.getEastNorth();
	        	mean_east += en.east();
	        	mean_north += en.north();
	        }
	        double n = (double)inputData.size();
	        mean_east /= n;
	        mean_north /= n;
	        
	        // get covariance matrix
	        double ca=0.0, cb=0.0, cc=0.0, cd=0.0;
	        double deast, dnorth;
	        for (CachedLatLon cll : inputData)
	        {
	        	EastNorth en = cll.getEastNorth();
	        	deast = en.east()-mean_east;
	        	dnorth = en.north()-mean_north;
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
	        double T = ca+cd, D = ca*cd-cb*cc; // trace, determinant
	        double variance1 = 0.5*T + Math.sqrt(0.25*T*T-D); // Eigenvalue 1
	        double variance2 = 0.5*T - Math.sqrt(0.25*T*T-D); // Eigenvalue 2
	        double evec1_east = (variance1-cd), evec1_north = cc; // eigenvec1
	        double evec2_east = (variance2-cd), evec2_north = cc; // eigenvec2
	        
	        double evec1_fac = Math.sqrt(variance1)/Math.sqrt(evec1_east*evec1_east+evec1_north*evec1_north);
	        double evec2_fac = Math.sqrt(variance2)/Math.sqrt(evec2_east*evec2_east+evec2_north*evec2_north);
	        double sigma1_east = evec1_east * evec1_fac, sigma1_north = evec1_north * evec1_fac;
	        double sigma2_east = evec2_east * evec2_fac, sigma2_north = evec2_north * evec2_fac;                     
	      
	       // save latlon coords of the mean and the ends of the crosshairs
	       Projection proj = Main.getProjection();
	       mean = new CachedLatLon(proj.eastNorth2latlon(new EastNorth(mean_east, mean_north)));
	       hair1Coord1 = new CachedLatLon(proj.eastNorth2latlon(
	    		   new EastNorth(mean_east-sigma1_east*fac, mean_north-sigma1_north*fac)));
	       hair1Coord2 = new CachedLatLon(proj.eastNorth2latlon(
	    		   new EastNorth(mean_east+sigma1_east*fac, mean_north+sigma1_north*fac)));
	       hair2Coord1 = new CachedLatLon(proj.eastNorth2latlon(
	    		   new EastNorth(mean_east-sigma2_east*fac, mean_north-sigma2_north*fac)));
	       hair2Coord2 = new CachedLatLon(proj.eastNorth2latlon(
	    		   new EastNorth(mean_east+sigma2_east*fac, mean_north+sigma2_north*fac)));
	       double efac = fac/Math.sqrt(inputData.getNDays());
	       // TopLeft, TopRight, BottomLeft in frame where sigma1=R sigma2=Top
	       ellipseCoord1 = new CachedLatLon(proj.eastNorth2latlon(
	    		   new EastNorth(mean_east+(-sigma1_east+sigma2_east)*efac, mean_north+(-sigma1_north+sigma2_north)*efac))); // 
	       ellipseCoord2 = new CachedLatLon(proj.eastNorth2latlon(
	    		   new EastNorth(mean_east+(sigma1_east+sigma2_east)*efac, mean_north+(sigma1_north+sigma2_north)*efac))); // 
	       ellipseCoord3 = new CachedLatLon(proj.eastNorth2latlon(
	    		   new EastNorth(mean_east+(-sigma1_east-sigma2_east)*efac, mean_north+(-sigma1_north-sigma2_north)*efac))); // 
	}

	public void paint(Graphics2D g, MapView mv)
	{
		g.setColor(Color.GREEN);
		g.setPaintMode();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2.0f));
		Point hair1Point1 = mv.getPoint(hair1Coord1.getEastNorth());
		Point hair1Point2 = mv.getPoint(hair1Coord2.getEastNorth());
		Point hair2Point1 = mv.getPoint(hair2Coord1.getEastNorth());
		Point hair2Point2 = mv.getPoint(hair2Coord2.getEastNorth());		
		g.drawLine(hair1Point1.x, hair1Point1.y, hair1Point2.x, hair1Point2.y);
		g.drawLine(hair2Point1.x, hair2Point1.y, hair2Point2.x, hair2Point2.y);
		
		Point2D meanPoint = mv.getPoint2D(mean.getEastNorth());
		Point2D ellipsePoint1 = mv.getPoint2D(ellipseCoord1.getEastNorth());
		Point2D ellipsePoint2 = mv.getPoint2D(ellipseCoord2.getEastNorth());
		Point2D ellipsePoint3 = mv.getPoint2D(ellipseCoord3.getEastNorth());
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
