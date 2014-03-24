// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;

public interface IAllKnowingTrashHeap {

    /**
     * Gets the list containing the best matching (closest) street names.
     *
     * @param name the name of the street to find the matches for.
     * @param maxEntries the maximum number of matches.
     * @return the closest street names
     */
    public List<String> getClosestStreetNames(String name, int maxEntries);

    /**
     * Gets the closest street name to the given name.
     *
     * @param name the name of the street to find a match for.
     * @return the closest street name
     */
    public String getClosestStreetName(String name);

    /**
     * Checks if the given street name is valid.
     *
     * @param name the name of the street to check.
     * @return true, if street name is valid; otherwise false.
     */
    public boolean isValidStreetName(String name);
}
