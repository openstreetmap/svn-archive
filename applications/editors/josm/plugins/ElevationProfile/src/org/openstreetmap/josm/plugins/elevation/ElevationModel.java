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

package org.openstreetmap.josm.plugins.elevation;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxRoute;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * Represents the top-level part of the elevation model. The elevation model
 * breaks done into the tracks/routes of a GPX file (see
 * {@link ElevationProfileNode}). Each track is divided into 'slices' (see
 * {@link ElevationProfileLeaf}) - a set of fixed number of way points. This
 * structure allows as well an overview over a single track as a detailed
 * elevation view of a track part.
 * 
 * @see ElevationProfileNode
 * @see ElevationProfileLeaf
 * @see IElevationModelTrackListener
 * @see IElevationModelSliceListener
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public class ElevationModel extends ElevationProfileBase implements IGpxVisitor {
	// private int sliceSize;
	private int trackCounter;
	private GpxData gpxData;

	private List<IElevationProfile> tracks;
	private List<WayPoint> buffer = new ArrayList<WayPoint>(1000);
	private List<WayPoint> tmpWaypoints = new ArrayList<WayPoint>(1000);

	private List<IElevationModelListener> listeners = new ArrayList<IElevationModelListener>();

	public ElevationModel() {
		this("", null, 100);
	}

	public ElevationModel(String name, GpxData data, int sliceSize) {
		super(name);
		gpxData = data;
		setSliceSize(Math.max(sliceSize, 100));
	}

	@Override
	public void setSliceSize(int sliceSize) {

		super.setSliceSize(sliceSize);

		// FIXME: Listener should go in base class
		updateElevationData();
		fireModelChanged();
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
	protected List<IElevationProfile> getTracks() {
		return tracks;
	}

	/**
	 * Gets a flag indicating whether the associated way points contained
	 * elevation data or not. This is the case if min and max height or both
	 * zero.
	 * 
	 * @return
	 */
	public boolean hasElevationData() {
		return getMaxHeight() != getMinHeight();
	}

	/**
	 * Fires the 'model changed' event to all listeners.
	 */
	protected void fireModelChanged() {
		for (IElevationModelListener listener : listeners) {
			listener.elevationProfileChanged(this);
		}
	}

	/**
	 * Adds a model listener to this instance.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addModelListener(IElevationModelListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes a model listener from this instance.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeModelListener(IElevationModelListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Removes all listeners from this instance.
	 */
	public void removeAllListeners() {
		this.listeners.clear();
	}

	/**
	 * (Re)computes the elevation model.
	 */
	private void computeProfile() {
		if (gpxData == null)
			return; // nothing to do

		trackCounter = 0;

		super.updateValues();
		if (tracks == null) {
			tracks = new ArrayList<IElevationProfile>();
		} else {
			tmpWaypoints.clear();
			buffer.clear();
			tracks.clear();
		}

		setDistance(gpxData.length());	// get distance from GPX 
		GpxIterator.visit(gpxData, this);

		// reduce data
		setWayPoints(WayPointHelper.downsampleWayPoints(tmpWaypoints,
				getSliceSize()), false);
	}

	/**
	 * Forces the model to refresh itself. Clients (e. g. UI widgets) may use
	 * this method to notify the model on UI changes.
	 */
	public void updateElevationData() {
		computeProfile();
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
		processWayPoint(wp);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.ElevationProfileBase#visit(org.openstreetmap.josm.data.gpx.WayPoint)
	 */
	@Override
	public void visit(WayPoint wp) {
		super.visit(wp);
		processWayPoint(wp);
	}

	public void start() {
		buffer.clear();
	}

	public void end() {
		String trackName = "Track#" + trackCounter;
		addTrackOrRoute(trackName);		
	}
	
	private void addTrackOrRoute(String trackName) {
		if (getSliceSize() > 0) {
			ElevationProfileNode emt = new ElevationProfileNode(trackName,
					this, buffer, getSliceSize());
			tracks.add(emt);
		}
		trackCounter++;
		buffer.clear();
	}

	private void processWayPoint(WayPoint wp) {
		if (wp == null) {
			throw new RuntimeException("WPT must not be null!");
		}
		
		buffer.add(wp);
		tmpWaypoints.add(wp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openstreetmap.josm.plugins.elevation.ElevationProfileBase#getChildren
	 * ()
	 */
	@Override
	public List<IElevationProfile> getChildren() {
		return tracks;
	}
}
