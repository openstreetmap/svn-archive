/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.elevation.gpx;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxRoute;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.elevation.IElevationModel;
import org.openstreetmap.josm.plugins.elevation.IElevationModelListener;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * Represents the top-level part of the elevation model. The elevation model
 * breaks done into the tracks/routes of a GPX file.
 * 
 * @see IElevationModelTrackListener
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public class ElevationModel implements IGpxVisitor, IElevationModel {
	// private int sliceSize;
	private int trackCounter;
	private GpxData gpxData;
	private String name;
	private WayPointMap children = new WayPointMap(); 
	private List<IElevationModelListener> listeners = new ArrayList<IElevationModelListener>();
	private List<WayPoint> buffer = new ArrayList<WayPoint>();
	private int currentProfileIndex = 0;
	private ElevationProfileBase curProfile = null;

	/**
	 * Instantiates a new elevation model.
	 */
	public ElevationModel() {
		this("", null);
	}

	/**
	 * Instantiates a new elevation model.
	 *
	 * @param name the name of the model
	 * @param data the GPX data
	 */
	public ElevationModel(String name, GpxData data) {
		gpxData = data;
		this.name = name;
		GpxIterator.visit(data, this);		
	}

	/**
	 * Gets the GPX data instance used by this model.
	 * 
	 * @return
	 */
	public GpxData getGpxData() {
		return gpxData;
	}
	
	/**
	 * @return the tracks
	 */
	protected WayPointMap getTracks() {
		return children;
	}

	/**
	 * Fires the 'model changed' event to all listeners.
	 */
	protected void fireModelChanged() {	    	
		for (IElevationModelListener listener : listeners) {
		    if (children != null && children.size() > 0)
			listener.elevationProfileChanged(getCurrentProfile());
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IElevationModel#addModelListener(org.openstreetmap.josm.plugins.elevation.IElevationModelListener)
	 */
	@Override
	public void addModelListener(IElevationModelListener listener) {
		this.listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IElevationModel#removeModelListener(org.openstreetmap.josm.plugins.elevation.IElevationModelListener)
	 */
	@Override
	public void removeModelListener(IElevationModelListener listener) {
		this.listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IElevationModel#removeAllListeners()
	 */
	@Override
	public void removeAllListeners() {
		this.listeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openstreetmap.josm.plugins.elevation.IGpxVisitor#visit(org.openstreetmap
	 * .josm.data.gpx.GpxRoute, org.openstreetmap.josm.data.gpx.WayPoint)
	 */
	public void visit(GpxRoute route, WayPoint wp) {
		processWayPoint(wp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openstreetmap.josm.plugins.elevation.IGpxVisitor#visit(org.openstreetmap
	 * .josm.data.gpx.GpxTrack, org.openstreetmap.josm.data.gpx.GpxTrackSegment,
	 * org.openstreetmap.josm.data.gpx.WayPoint)
	 */
	public void visit(GpxTrack track, GpxTrackSegment segment, WayPoint wp) {
	    	// we ignore the segment here 
		processWayPoint(wp);
		
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.ElevationProfileBase#visit(org.openstreetmap.josm.data.gpx.WayPoint)
	 */
	@Override
	public void visit(WayPoint wp) {
		processWayPoint(wp);
	}

	public void start() {
		curProfile = new ElevationProfileBase(name);		
	}

	public void end() {
		String trackName = name; //gpxData.getString(GpxData.META_NAME);// "Track#" + trackCounter;
		
		if (trackCounter > 0) {
		    trackName += "." + trackCounter;
		}
		addTrackOrRoute(trackName);	
		trackCounter++;
	}
	

	@Override
	public void start(GpxTrack track) {
	    curProfile = new ElevationProfileBase(name);	    
	}

	@Override
	public void end(GpxTrack track) {
	    if (curProfile == null) throw new RuntimeException("Internal error: No elevation profile");
	    
	    curProfile.setDistance(track.length());
	    addTrackOrRoute(name);	    
	}
	
	@Override
	public void start(GpxTrack track, GpxTrackSegment segment) {
	    // Nothing to do here for now
	}

	@Override
	public void end(GpxTrack track, GpxTrackSegment segment) {
	    // Nothing to do here for now
	}

	
	/**
	 * Adds a track or route to the internal track list.
	 *
	 * @param trackName the track name
	 */
	private void addTrackOrRoute(String trackName) {
	    	if (buffer.size() > 0) {        	    	
        		curProfile.setWayPoints(buffer);
        		curProfile.setName(trackName);
        		children.add(curProfile);
	    	}
	}

	private void processWayPoint(WayPoint wp) {
		if (wp == null) {
			throw new RuntimeException("WPT must not be null!");
		}
		
		buffer.add(wp);
	}

	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IElevationModel#getProfiles()
	 */
	@Override
	public List<IElevationProfile> getProfiles() {
		return children;
	}

	@Override
	public IElevationProfile getCurrentProfile() {
	    if (currentProfileIndex < 0 || currentProfileIndex >= profileCount()) return null;
	    
	    return children.get(currentProfileIndex);
	}

	@Override
	public void setCurrentProfile(IElevationProfile newProfile) {
	    CheckParameterUtil.ensureParameterNotNull(newProfile);
	    
	    if (!children.contains(newProfile)) {
		children.add(newProfile);
	    }
	    
	    setCurrentProfile(children.indexOf(newProfile)); 
	}

	@Override
	public void setCurrentProfile(int index) {
	    if (index < 0 || index >= profileCount()) throw new RuntimeException("Invalid arg for setCurrentProfile: " + index + ", value must be 0.." + profileCount());
	    
	    currentProfileIndex = index;
	    fireModelChanged();	    
	}

	@Override
	public int profileCount() {
	    return children != null ? children.size() : 0;
	}
}
