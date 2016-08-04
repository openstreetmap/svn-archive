// License: GPL. For details, see LICENSE file.
package com.innovant.josm.jrt.osm;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Class that represents an edge of the graph.
 * @author jose
 */
public class OsmEdge extends DefaultWeightedEdge {
    /**
     * Serial
     */
    private static final long serialVersionUID = 1L;
    /**
     * Way associated
     */
    private Way way;
    /**
     * Nodes in the edge
     */
    private Node from, to;
    /**
     * Length edge
     */
    private double length;
    /**
     * Speed edge.
     */
    private double speed;

    /**
     * Constructor
     */
    public OsmEdge(Way way, Node from, Node to) {
        super();
        this.way = way;
        this.from = from;
        this.to = to;
        this.length = from.getCoor().greatCircleDistance(to.getCoor());
    }

    /**
     * @return the way
     */
    public Way getWay() {
        return this.way;
    }

    public EastNorth fromEastNorth() {
        return this.from.getEastNorth();
    }

    public EastNorth toEastNorth() {
        return this.to.getEastNorth();
    }

    /**
     * Returns length of segment in meters
     * @return length of segment in meters.
     */
    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
