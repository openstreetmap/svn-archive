// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation;

import java.util.List;

public interface IElevationModel {

    /**
     * Adds a model listener to this instance.
     *
     * @param listener
     *            The listener to add.
     */
    void addModelListener(IElevationModelListener listener);

    /**
     * Removes a model listener from this instance.
     *
     * @param listener
     *            The listener to remove.
     */
    void removeModelListener(IElevationModelListener listener);

    /**
     * Removes all listeners from this instance.
     */
    void removeAllListeners();

    /**
     * Gets the elevation profiles. In most GPX files there may by
     *
     * @return the profiles
     */
    List<IElevationProfile> getProfiles();

    /**
     * Gets the current profile.
     *
     * @return the current profile
     */
    IElevationProfile getCurrentProfile();

    /**
     * Sets the current profile.
     *
     * @param newProfile the new current profile
     */
    void setCurrentProfile(IElevationProfile newProfile);

    /**
     * Sets the current profile by index.
     *
     * @param index the new current profile. Valied numbers are 0 to (profileCount - 1)
     */
    void setCurrentProfile(int index);

    /**
     * Gets the number of elevation profiles within the model.
     *
     * @return the int
     */
    int profileCount();
}
