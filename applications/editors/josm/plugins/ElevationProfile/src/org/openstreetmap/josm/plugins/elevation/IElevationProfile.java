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

import java.util.Date;
import java.util.List;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Interface for an elevation profile providing special properties/values.
 */
public interface IElevationProfile {
	/**
	 * Gets the name of the elevation profile.
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Gets the time stamp of first recorded track point.
	 * @return
	 */
	public abstract Date getStart();

	/**
	 * Gets the time stamp of last recorded track point.
	 * @return
	 */
	public abstract Date getEnd();
	
	/**
	 * Gets the minimum elevation height of all tracks and routes.
	 * @return
	 */
	public abstract int getMinHeight();

	/**
	 * Gets the maximum elevation height of all tracks and routes.
	 * @return
	 */
	public abstract int getMaxHeight();
	
	/**
	 * Gets the distance of the track in kilometers.
	 */
	public abstract double getDistance();
	
	/**
	 * Gets the average elevation height of all tracks and routes.
	 * @return
	 */
	public abstract int getAverageHeight();
	
	/**
	 * Gets the difference between min and max elevation.
	 * @return
	 */
	public int getHeightDifference();
	
	/**
	 * Gets the elevation gain.
	 * 
	 * @return
	 */
	public int getGain();

	/**
	 * Gets the total number of way points (sum of all way points of all tracks and routes). 
	 * @return
	 */
	public abstract int getNumberOfWayPoints();
	
	/**
	 * Gets the list containing the way points.
	 * @return
	 */
	public List<WayPoint> getWayPoints();
	
	/**
	 * Gets the first recorded way point.
	 * @return
	 */
	public WayPoint getStartWayPoint();
	
	/**
	 * Gets the last recorded way point.
	 * @return
	 */
	public WayPoint getEndWayPoint();
	
	/**
	 * Gets the way point with the highest elevation value.
	 * @return
	 */
	public WayPoint getMaxWayPoint();
	
	/**
	 * Gets the way point with the lowest elevation value.
	 * @return
	 */
	public WayPoint getMinWayPoint();
	
	/**
	 * Gets a flag indicating whether the associated way points 
	 * contained elevation data or not. This is the case if min
	 * and max height are equal.
	 * @return
	 */
	public boolean hasElevationData();

	/**
	 * Returns the time between start and end of the track.
	 * @return
	 */
	public long getTimeDifference();
	
	/**
	 * Gets the elevation value for at the given data index point.
	 */
	public int elevationValueAt(int i);
	
	/**
	 * Gets the coordinate bounds of the elevation profile.
	 *
	 * @return the bounds
	 */
	public Bounds getBounds();
	
	/**
	 * Gets the children of the segment (maybe null).
	 */
	public List<IElevationProfile> getChildren();
	
	/**
	 * Gets the parent of the elevation profile.
	 */
	public IElevationProfile getParent();
	
	/**
	 * Triggers model refresh.
	 */
	public void updateElevationData();
}