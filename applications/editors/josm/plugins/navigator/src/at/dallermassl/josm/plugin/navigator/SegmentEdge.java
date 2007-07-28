/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @author cdaller
 * 
 */
public class SegmentEdge extends DefaultWeightedEdge {
    private Segment segment;
    private boolean inverted;
    private Way way;

    public SegmentEdge() {
        super();
        System.out.println("warning: edge without segment!");
    }

    /**
     * @param segment
     */
    public SegmentEdge(Segment segment) {
        this(segment, false);
    }

    /**
     * @param segment
     * @param inverted
     *            if <code>true</code> the edge is the other direction as the contained segment.
     */
    public SegmentEdge(Segment segment, boolean inverted) {
        super();
        this.segment = segment;
        this.inverted = inverted;
    }

    /**
     * @return the segment
     */
    public Segment getSegment() {
        return this.segment;
    }

    /**
     * @return the way
     */
    public Way getWay() {
        return this.way;
    }

    /**
     * @param way
     *            the way to set
     */
    public void setWay(Way way) {
        this.way = way;
    }

    /**
     * @return the inverted
     */
    public boolean isInverted() {
        return this.inverted;
    }

    /**
     * Returns length of segment in meters (correct only on equator!)
     * @return length of semgent in meters.
     */
    public double getLengthInM() {
        return Math.sqrt(segment.from.coor.distance(segment.to.coor)) * 111000;
    }

}
