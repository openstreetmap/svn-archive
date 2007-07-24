/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;

/**
 * @author cdaller
 *
 */
public class WayEdge {
  private long length;
  private Node startNode;
  private Node endNode;
  private List<Segment> segments;
  
  public WayEdge() {
    
  }
  
  public WayEdge(int length) {
    this.length = length;
  }

  /**
   * @return the length
   */
  public long getLength() {
    return this.length;
  }

  /**
   * @param length the length to set
   */
  public void setLength(long length) {
    this.length = length;
  }

  /**
   * @return the segments
   */
  public List<Segment> getSegments() {
    return this.segments;
  }

  /**
   * @param segments the segments to set
   */
  public void setSegments(List<Segment> segments) {
    this.segments = segments;
  }

  /**
   * @return the startNode
   */
  public Node getStartNode() {
    return this.startNode;
  }

  /**
   * @param startNode the startNode to set
   */
  public void setStartNode(Node startNode) {
    this.startNode = startNode;
  }

  /**
   * @return the endNode
   */
  public Node getEndNode() {
    return this.endNode;
  }

  /**
   * @param endNode the endNode to set
   */
  public void setEndNode(Node endNode) {
    this.endNode = endNode;
  }
  
  

}
