// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.pointinfo;

import org.openstreetmap.josm.data.coor.LatLon;

public abstract class AbstractPointInfoModule {

    /**
     * Return Html text representation
     * @return String htmlText
     */
    public abstract String getHtml();

    /**
     * Perform given action
     *  e.g.: copy tags to clipboard
     * @param act Action to be performed
     */
    public abstract void performAction(String act);

    /**
     * Get a information about given position.
     * @param pos Position on the map
     */
    public abstract void prepareData(LatLon pos);

    /**
     * Returns the public name of this module
     * @return String name
     */
    public abstract String getName();

    /**
     * Returns the name of the area (country, state) where this module is valid
     * @return String areaName
     */
    public abstract String getArea();
}
