package org.openstreetmap.josm.plugins.elevation;

import java.util.List;


public interface IElevationModel {

    /**
     * Adds a model listener to this instance.
     * 
     * @param listener
     *            The listener to add.
     */
    public abstract void addModelListener(IElevationModelListener listener);

    /**
     * Removes a model listener from this instance.
     * 
     * @param listener
     *            The listener to remove.
     */
    public abstract void removeModelListener(IElevationModelListener listener);

    /**
     * Removes all listeners from this instance.
     */
    public abstract void removeAllListeners();

    /**
     * Gets the elevation profiles. In most GPX files there may by
     *
     * @return the profiles
     */
    public abstract List<IElevationProfile> getProfiles();
    
    /**
     * Gets the current profile.
     *
     * @return the current profile
     */
    public abstract IElevationProfile getCurrentProfile();
    
    /**
     * Sets the current profile.
     *
     * @param newProfile the new current profile
     */
    public abstract void setCurrentProfile(IElevationProfile newProfile);
    
    /**
     * Sets the current profile by index.
     *
     * @param index the new current profile. Valied numbers are 0 to (profileCount - 1)
     */
    public abstract void setCurrentProfile(int index);
    
    /**
     * Gets the number of elevation profiles within the model.
     *
     * @return the int
     */
    public abstract int profileCount();

}