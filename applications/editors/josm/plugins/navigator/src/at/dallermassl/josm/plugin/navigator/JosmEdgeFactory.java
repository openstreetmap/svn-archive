/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import org.jgrapht.EdgeFactory;
import org.openstreetmap.josm.data.osm.Node;

/**
 * @author cdaller
 *
 */
public class JosmEdgeFactory implements EdgeFactory<Node, WayEdge> {

  

  /* (non-Javadoc)
   * @see org.jgrapht.EdgeFactory#createEdge(java.lang.Object, java.lang.Object)
   */
  public WayEdge createEdge(Node sourceVertex, Node targetVertex) {
    throw new RuntimeException("not implemented");
  }


}
