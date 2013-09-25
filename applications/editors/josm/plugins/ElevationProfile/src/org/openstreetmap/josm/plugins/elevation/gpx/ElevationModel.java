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
import org.openstreetmap.josm.data.gpx.IWithAttributes;
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
	private WayPointMap profiles = new WayPointMap(); 
	private List<IElevationModelListener> listeners = new ArrayList<IElevationModelListener>();
	private List<WayPoint> buffer = new ArrayList<WayPoint>();
	private int currentProfileIndex = 0;
	private ElevationProfile curProfile = null;

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
		return profiles;
	}

	/**
	 * Fires the 'model changed' event to all listeners.
	 */
	protected void fireModelChanged() {	    	
		for (IElevationModelListener listener : listeners) {
		    if (profiles != null && profiles.size() > 0)
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IElevationModel#getProfiles()
	 */
	@Override
	public List<IElevationProfile> getProfiles() {
		return profiles;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.IElevationModel#getCurrentProfile()
	 */
	@Override
	public IElevationProfile getCurrentProfile() {
	    if (currentProfileIndex < 0 || currentProfileIndex >= profileCount()) return null;
	    
	    return profiles.get(currentProfileIndex);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.IElevationModel#setCurrentProfile(org.openstreetmap.josm.plugins.elevation.IElevationProfile)
	 */
	@Override
	public void setCurrentProfile(IElevationProfile newProfile) {
	    CheckParameterUtil.ensureParameterNotNull(newProfile);
	    
	    if (!profiles.contains(newProfile)) {
		profiles.add(newProfile);
	    }
	    
	    setCurrentProfile(profiles.indexOf(newProfile)); 
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.IElevationModel#setCurrentProfile(int)
	 */
	@Override
	public void setCurrentProfile(int index) {
	    if (index < 0 || index >= profileCount()) throw new RuntimeException("Invalid arg for setCurrentProfile: " + index + ", value must be 0.." + profileCount());
	    
	    currentProfileIndex = index;
	    fireModelChanged();	    
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.IElevationModel#profileCount()
	 */
	@Override
	public int profileCount() {
	    return profiles != null ? profiles.size() : 0;
	}

	// Visitor stuff starts here...
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#beginWayPoints()
	 */
	public void beginWayPoints() {
	    // we ignore single way points (elevation profile is quite meaningless...)
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#endWayPoints()
	 */
	public void endWayPoints() {		
	    // we ignore single way points (elevation profile is quite meaningless...)
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.ElevationProfileBase#visit(org.openstreetmap.josm.data.gpx.WayPoint)
	 */
	@Override
	public void visitWayPoint(WayPoint wp) {
	    // we ignore single way points (elevation profile is quite meaningless...)
	}


	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#beginTrack(org.openstreetmap.josm.data.gpx.GpxTrack)
	 */
	@Override
	public void beginTrack(GpxTrack track) {
	    createProfile(track);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#endTrack(org.openstreetmap.josm.data.gpx.GpxTrack)
	 */
	@Override
	public void endTrack(GpxTrack track) {
	    if (curProfile == null) throw new RuntimeException("Internal error: No elevation profile");
	    
	    curProfile.setDistance(track.length());
	    commitProfile();
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#beginTrackSegment(org.openstreetmap.josm.data.gpx.GpxTrack, org.openstreetmap.josm.data.gpx.GpxTrackSegment)
	 */
	@Override
	public void beginTrackSegment(GpxTrack track, GpxTrackSegment segment) {
	    // Nothing to do here for now
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#endTrackSegment(org.openstreetmap.josm.data.gpx.GpxTrack, org.openstreetmap.josm.data.gpx.GpxTrackSegment)
	 */
	@Override
	public void endTrackSegment(GpxTrack track, GpxTrackSegment segment) {
	    // Nothing to do here for now
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#visitTrackPoint(org.openstreetmap.josm.data.gpx.WayPoint, org.openstreetmap.josm.data.gpx.GpxTrack, org.openstreetmap.josm.data.gpx.GpxTrackSegment)
	 */
	@Override
	public void visitTrackPoint(WayPoint wp, GpxTrack track,
		GpxTrackSegment segment) {
	    
	    processWayPoint(wp);	    
	}
	

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#beginRoute(org.openstreetmap.josm.data.gpx.GpxRoute)
	 */
	@Override
	public void beginRoute(GpxRoute route) {
	    createProfile(route);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#endRoute(org.openstreetmap.josm.data.gpx.GpxRoute)
	 */
	@Override
	public void endRoute(GpxRoute route) {
	    if (curProfile == null) throw new RuntimeException("Internal error: No elevation profile");
	    // a GpxRoute has no 'length' property 
	    curProfile.setDistance(0);
	    commitProfile();
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.gpx.IGpxVisitor#visitRoutePoint(org.openstreetmap.josm.data.gpx.WayPoint, org.openstreetmap.josm.data.gpx.GpxRoute)
	 */
	@Override
	public void visitRoutePoint(WayPoint wp, GpxRoute route) {
	    processWayPoint(wp);	    
	}
	
	/**
	 * Creates a new profile.
	 *
	 * @param trackOrRoute the track or route
	 */
	private void createProfile(IWithAttributes trackOrRoute) {
	    // check GPX data 
	    String trackName = (String) trackOrRoute.get("name");
	    
	    // no name given, build artificial one
	    if (trackName == null) {
		trackName = (String) trackOrRoute.get(GpxData.META_NAME);
		if (trackName == null) {
		    trackName = name + "." + trackCounter;
		}
	    }
	    
	    curProfile = new ElevationProfile(trackName);
	}
	
	/**
	 * Adds a track or route to the internal track list.
	 *
	 * @param trackName the track name
	 */
	private void commitProfile() {
	    	if (buffer.size() > 0) {    
	    	    	// assign way points to profile...
        		curProfile.setWayPoints(buffer);
        		// ... and add to profile list
        		profiles.add(curProfile);
        		buffer.clear();
	    	}
	}

	/**
	 * Adds the given way point to the current buffer.
	 *
	 * @param wp the wp
	 */
	private void processWayPoint(WayPoint wp) {
		if (wp == null) {
			throw new RuntimeException("WPT must not be null!");
		}
		
		buffer.add(wp);
	}
}
