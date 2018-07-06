// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;

/**
 * @author renerr18
 *
 *
 */
public class StreetsideCubemap extends StreetsideAbstractImage {

	/**
	* If two values for field cd differ by less than EPSILON both values are considered equal.
	*/
	@SuppressWarnings("unused")
	private static final float EPSILON = 1e-5f;

	/**
	 * Main constructor of the class StreetsideCubemap
	 *
	 * @param quadId
	 *            The Streetside id of the base frontal image of the cubemap
	 *            in quternary
	 * @param latLon
	 *            The latitude and longitude where it is positioned.
	 * @param he
	 *            The direction of the images in degrees, meaning 0 north (camera
	 *            direction is not yet supported in the Streetside plugin).
	 */
   public StreetsideCubemap(String quadId, LatLon latLon, double he) {
	   super(quadId, latLon, he);
	 }

	/**
	 * Comparison method for the StreetsideCubemap object.
	 *
	 * @param image
	 *            - a StreetsideAbstract image object
	 *
	 *            StreetsideCubemaps are considered equal if they are associated
	 *            with the same image id - only one cubemap may be displayed at a
	 *            time. If the image selection changes, the cubemap changes.
	 *
	 * @return result of the hashcode comparison.
	 * @see org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage
	 */
	@Override
	public int compareTo(StreetsideAbstractImage image) {
		if (image instanceof StreetsideImage) {
			return id.compareTo(((StreetsideImage) image).getId());
		}
		return hashCode() - image.hashCode();
	}

	/**
	 * HashCode StreetsideCubemap object.
	 *
	 * @return int hashCode
	 * @see org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage
	 */
    @Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * stops ImageDisplay WalkAction (not currently supported by Streetside)
	 *
	 * @see org.openstreetmap.josm.plugins.streetside.actions.StreetsideWalkAction
	 */
	@Override
	public void stopMoving() {
		super.stopMoving();
	}

	/**
	 * turns ImageDisplay WalkAction (not currently supported by Streetside)
	 *
	 * @param he - the direction the camera is facing (heading)
	 *
	 * @see org.openstreetmap.josm.plugins.streetside.actions.StreetsideWalkAction
	 */
	@Override
	public void turn(double he) {
		super.turn(he);
	}

	/**
	 * @return the height of an assembled cubemap face for 16-tiled or 4-tiled imagery
	 *
	 * @see org.openstreetmap.josm.plugins.streetside.actions.StreetsideWalkAction
	 */
	public int getHeight() {
		return StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get().booleanValue()?1016:510;
	}

}