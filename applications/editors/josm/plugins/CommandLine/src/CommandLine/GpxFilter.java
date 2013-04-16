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
	private GpxData data;

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
			//System.out.println("New track");
			currentTrack = new ArrayList<Collection<WayPoint>>();
			for (GpxTrackSegment segment : track.getSegments()) {
				//System.out.println("New segment");
				currentSegment = new ArrayList<WayPoint>();
				for (WayPoint wp : segment.getWayPoints()) {
					//System.out.println("Point " + String.valueOf(wp.getCoor().getX()) + ", " + String.valueOf(wp.getCoor().getY()) + " situaded in bbox? " + String.valueOf(bbox.bounds(wp.getCoor())) );
					if ( bbox.bounds(wp.getCoor()) ) {
						currentSegment.add(wp);
					} else {
						if (currentSegment.size() > 1) {
							currentTrack.add(currentSegment);
							currentSegment = new ArrayList<WayPoint>();
						}
					}
				}
				if (currentSegment.size() > 1) {
					currentTrack.add(currentSegment);
					currentSegment = new ArrayList<WayPoint>();
				}
			}
			this.data.tracks.add( new ImmutableGpxTrack( currentTrack, Collections.<String, Object>emptyMap()) );
		}
	}

	public GpxData getGpxData() {
		return data;
	}
}
