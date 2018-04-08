// License: GPL. For details, see LICENSE file.
package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.WaySegment;

/**
 * Distance from platform to ways
 * 
 * @author Rodion Scherbakov
 */
public class NearestWaySegment {
    /**
     * Square of distance
     */
    public Double distanceSq = 1000000000.0;
    /**
     * Way segment
     */
    public WaySegment waySegment;
    /**
     * Node
     */
    public Node newNode;

    /**
     * Constructor
     * 
     * @param distanceSq Square of distance
     * @param waySegment Way segment
     * @param newNode Node
     */
    public NearestWaySegment(Double distanceSq, WaySegment waySegment, Node newNode) {
        this.distanceSq = distanceSq;
        this.waySegment = waySegment;
        this.newNode = newNode;
    }
}
