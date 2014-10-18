// License: GPL. For details, see LICENSE file.
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
	private final GpxData gpxData;
	private final String name;
	private final WayPointMap profiles = new WayPointMap();
	private final List<IElevationModelListener> listeners = new ArrayList<>();
	private final List<WayPoint> buffer = new ArrayList<>();
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

	@Override
	public void addModelListener(IElevationModelListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeModelListener(IElevationModelListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void removeAllListeners() {
		this.listeners.clear();
	}

	@Override
	public List<IElevationProfile> getProfiles() {
		return profiles;
	}

	@Override
	public IElevationProfile getCurrentProfile() {
		if (currentProfileIndex < 0 || currentProfileIndex >= profileCount()) return null;

		return profiles.get(currentProfileIndex);
	}

	@Override
	public void setCurrentProfile(IElevationProfile newProfile) {
		CheckParameterUtil.ensureParameterNotNull(newProfile);

		if (!profiles.contains(newProfile)) {
			profiles.add(newProfile);
		}

		setCurrentProfile(profiles.indexOf(newProfile));
	}

	@Override
	public void setCurrentProfile(int index) {
		if (index < 0 || index >= profileCount()) throw new RuntimeException("Invalid arg for setCurrentProfile: " + index + ", value must be 0.." + profileCount());

		currentProfileIndex = index;
		fireModelChanged();
	}

	@Override
	public int profileCount() {
		return profiles != null ? profiles.size() : 0;
	}

	// Visitor stuff starts here...

	@Override
	public void beginWayPoints() {
		// we ignore single way points (elevation profile is quite meaningless...)
	}

	@Override
	public void endWayPoints() {
		// we ignore single way points (elevation profile is quite meaningless...)
	}

	@Override
	public void visitWayPoint(WayPoint wp) {
		// we ignore single way points (elevation profile is quite meaningless...)
	}


	@Override
	public void beginTrack(GpxTrack track) {
		createProfile(track);
	}

	@Override
	public void endTrack(GpxTrack track) {
		if (curProfile == null) throw new RuntimeException("Internal error: No elevation profile");

		curProfile.setDistance(track.length());
		commitProfile();
	}

	@Override
	public void beginTrackSegment(GpxTrack track, GpxTrackSegment segment) {
		// Nothing to do here for now
	}

	@Override
	public void endTrackSegment(GpxTrack track, GpxTrackSegment segment) {
		// Nothing to do here for now
	}

	@Override
	public void visitTrackPoint(WayPoint wp, GpxTrack track,
			GpxTrackSegment segment) {

		processWayPoint(wp);
	}

	@Override
	public void beginRoute(GpxRoute route) {
		createProfile(route);
	}

	@Override
	public void endRoute(GpxRoute route) {
		if (curProfile == null) throw new RuntimeException("Internal error: No elevation profile");
		// a GpxRoute has no 'length' property
		curProfile.setDistance(0);
		commitProfile();
	}

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

		if (trackName == null) {
			trackName = (String) trackOrRoute.get(GpxData.META_NAME);
			if (trackName == null) {
				// no name given, build artificial one
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
