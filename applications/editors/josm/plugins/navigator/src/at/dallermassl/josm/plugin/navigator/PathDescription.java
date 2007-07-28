/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import org.openstreetmap.josm.data.osm.Way;

/**
 * @author cdaller
 *
 */
public class PathDescription {
    private Way way;
    private double length;
    /**
     * @param way
     * @param length
     */
    public PathDescription(Way way, double length) {
        super();
        this.way = way;
        this.length = length;
    }
    /**
     * @return the way
     */
    public Way getWay() {
        return this.way;
    }
    /**
     * @return the length
     */
    public double getLength() {
        return this.length;
    }

}
