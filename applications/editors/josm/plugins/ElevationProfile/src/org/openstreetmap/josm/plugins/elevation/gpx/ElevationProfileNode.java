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

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.ElevationHelper;

/**
 * Represents a elevation model of a single GPX track, route or collection of
 * way points. It usually breaks down into many slices represented by 
 * {@link ElevationProfileLeaf} which are accessible via {@link ElevationProfileBase#getChildren()}.
 * 
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 * @see ElevationProfileBase
 * @see ElevationModel
 * @see ElevationProfileLeaf
 */
public class ElevationProfileNode extends ElevationProfileBase {
	private List<IElevationProfile> slices;
	
	/**
	 * Creates a new elevation track model.
	 * @param trackName The name of the track.
	 * @param parent The parent elevation profile
	 * @param sliceSize The (maximum) size of each slice.
	 * @param wayPoints The way points belonging to this track.
	 */
	public ElevationProfileNode(String trackName, IElevationProfile parent, List<WayPoint> wayPoints, int sliceSize) {
		super(trackName, parent, wayPoints, sliceSize);
		setSliceSize(sliceSize);
		// ensure that sliceSize has a reasonable value
		createsSlices(wayPoints);
	}

	/**
	 * Notifies listeners that the active slice has been changed.
	 * 
	 * @param oldIndex
	 *            The index of the previously selected slice.
	 * @param newIndex
	 *            The index of the current slice.
	 * 
	 *            private void fireSliceChanged(int oldIndex, int newIndex) { if
	 *            (listeners == null || listeners.size() == 0) { return; }
	 * 
	 *            if (!checkIndex(newIndex)) { throw new
	 *            IndexOutOfBoundsException(
	 *            String.format("Invalid index: %d (expected 0-%d)", newIndex,
	 *            getNumberOfSlices())); } ElevationModelSlice oldObj =
	 *            getSliceAt(oldIndex); ElevationModelSlice newObj =
	 *            getSliceAt(newIndex);
	 * 
	 *            for (IElevationModelSliceListener listener : listeners) {
	 *            listener.sliceSelectionChanged(oldObj, newObj); } }
	 */

	/**
	 * Gets the number of slices.
	 * 
	 * @return
	 */
	public int getNumberOfSlices() {
		return slices != null ? slices.size() : 0;
	}

	/**
	 * Assign other way points to this track.
	 * 
	 * @param waypoints
	 *            The list containing the way points.
	 */
	public void updateTrack(List<WayPoint> waypoints) {
		createsSlices(waypoints);
	}

	/**
	 * Creates the track slices.
	 * 
	 * @param wayPoints
	 *            The way points of the track.
	 */
	private void createsSlices(List<WayPoint> wayPoints) {
		if (wayPoints == null || getSliceSize() <= 0) {
			return;
		}

		if (slices == null) {
			slices = new ArrayList<IElevationProfile>();
		} else {
			slices.clear();
		}

		for (int i = 0; i < wayPoints.size(); i += getSliceSize()) {
			int to = Math.min(i + getSliceSize(), wayPoints.size());
			ElevationProfileLeaf ems = new ElevationProfileLeaf(getName(), this, wayPoints.subList(i, to));
			slices.add(ems);
		}
		
		// downsample
		setWayPoints(ElevationHelper.downsampleWayPoints(wayPoints, getSliceSize()), false);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.elevation.ElevationProfileBase#getChildren()
	 */
	@Override
	public List<IElevationProfile> getChildren() {
		return slices;
	}
}
