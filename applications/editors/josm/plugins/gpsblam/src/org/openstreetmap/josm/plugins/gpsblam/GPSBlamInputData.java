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

import java.awt.Point;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;

public class GPSBlamInputData extends LinkedList<CachedLatLon> {
	private Collection<Calendar> datesSeen = new HashSet<Calendar>();

	public int getNDays() { return datesSeen.size(); }

	// select a set of GPX points and count how many tracks they have come from,
	// within given radius of line between given points
	public GPSBlamInputData(Point p1, Point p2, int radius)
	{
		// Build a list of waypoints in all visible layers -
		//	Collection<WayPoint> wayPoints = new LinkedList<WayPoint>();

		Collection<Layer> layers = Main.map.mapView.getAllLayers();
		for (Layer l : layers) 
		{      
			if (l.isVisible() && l instanceof GpxLayer)
			{
				for (GpxTrack track : ((GpxLayer)l).data.tracks)
				{
					//		 			System.out.println("TRACK");
					for (GpxTrackSegment segment: track.getSegments()) 
					{
						//		 				wayPoints.addAll(segment.getWayPoints());/*
						for (WayPoint wayPoint : segment.getWayPoints())
						{
							// 					points.add(Main.map.mapView.getPoint(wayPoint.getCoor().getEastNorth()));
							//		 					wayPoints.add(Main.map.mapView.getPoint(wayPoint.getCoor().getEastNorth()));

							if (p2.equals(p1)) // circular selection
							{
								CachedLatLon cll = new CachedLatLon(wayPoint.getCoor());
								Point p = Main.map.mapView.getPoint(cll.getEastNorth());
								if (p.distance(p1) < radius)
								{
									this.add(cll, wayPoint);
								}
							}
							else  // selection based on distance from line from p1 to p2
							{
								double length = Math.sqrt((p2.x-p1.x)*(p2.x-p1.x)+(p2.y-p1.y)*(p2.y-p1.y))+1e-5; // hack to work for zero length            
								double dir_x = (p2.x-p1.x)/length, dir_y = (p2.y-p1.y)/length; // unit direction vector from p1.x,p1.y to p2.x, p2.y
								double perpdir_x = dir_y, perpdir_y = -dir_x; // unit vector 90deg CW from direction vector

								CachedLatLon cll = new CachedLatLon(wayPoint.getCoor());
								Point p = Main.map.mapView.getPoint(cll.getEastNorth());
								double p_x = p.x-p1.x, p_y=p.y-p1.y; // vector from point clicked to waypoint
								double p_par = p_x*dir_x + p_y*dir_y; // parallel component
								double p_perp = p_x*perpdir_x + p_y*perpdir_y; // perpendicular component
								double pardist = 0.0;

								if (p_par < 0)
									pardist = -p_par;
								else if (p_par > length)
									pardist = p_par-length;

								double distsq = pardist*pardist + p_perp*p_perp;

								//        	System.out.printf("dist %.1f %.1f %.1f %.1f %.1f %.1f %.1f %.1f %.1f\n", Math.sqrt(distsq), p_x, p_y, p1.x, p1.y, p_par, p_perp, dir_x, dir_y);
								if (distsq < radius*radius)
								{
									this.add(cll, wayPoint);
								}
							} // end if circular else line based selection
						}// end loop over wayponts in segment
					} // end loop over segments in track
					
				}	// end loop over tracks in layer
			} // end if layer visible
		} // end loop over layers
	} // end constructor

	private void add(CachedLatLon cll, WayPoint wayPoint) {
		this.add(cll);
		Calendar day = new GregorianCalendar();
		day.setTimeInMillis((long)wayPoint.time*1000);
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		datesSeen.add(day);		
	}
}