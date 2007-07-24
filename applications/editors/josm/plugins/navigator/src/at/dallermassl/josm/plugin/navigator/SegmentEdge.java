/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.openstreetmap.josm.data.osm.Segment;

/**
 * @author cdaller
 *
 */
public class SegmentEdge extends DefaultWeightedEdge {
  private Segment segment;
  
  public SegmentEdge() {
    super();
    System.out.println("warning: edge without segment!");
  }

  /**
   * @param segment
   */
  public SegmentEdge(Segment segment) {
    super();
    this.segment = segment;
  }

  /**
   * @return the segment
   */
  public Segment getSegment() {
    return this.segment;
  }

}
