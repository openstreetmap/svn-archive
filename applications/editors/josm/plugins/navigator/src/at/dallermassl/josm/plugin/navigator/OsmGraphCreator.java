/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @author cdaller
 *
 */
public class OsmGraphCreator {
  private Map<Node, Set<Segment>> nodeSegmentMap = new HashMap<Node, Set<Segment>>();
  private Set<Segment> segments = new HashSet<Segment>();
  private Set<Way> ways = new HashSet<Way>();
  private Set<Node> crossingNodes = new HashSet<Node>();
  private List<WayEdge> edges = new ArrayList<WayEdge>();
  
  private Map<String, Double> highwayWeight;
  private static final double DEFAULT_WEIGHT = Double.MAX_VALUE;
  
  public OsmGraphCreator() {
    highwayWeight = new HashMap<String, Double>();
    highwayWeight.put("motorway", 130.0);
    highwayWeight.put("primary", 100.0);
    highwayWeight.put("secondary", 70.0);
    highwayWeight.put("unclassified", 50.0);
    highwayWeight.put("residential", 40.0);
    highwayWeight.put("footway", 1.0);
  }
  
  public Graph<Node, SegmentEdge> createSegmentGraph() {
    SimpleDirectedWeightedGraph<Node, SegmentEdge> graph = new SimpleDirectedWeightedGraph<Node, SegmentEdge>(SegmentEdge.class);
    //SimpleGraph<Node, SegmentEdge> graph = new SimpleGraph<Node, SegmentEdge>(SegmentEdge.class);
    SegmentEdge edge;
    double weight;
    // iterate all ways and segments for all nodes:
    for(Way way : Main.ds.ways) {
      for(Segment segment : way.segments) {
        if(segment.from.id == 21100429 | segment.to.id == 21100429) {
          System.out.println("loggin tegetthoff/radetzky for way " + way.get("name") + ", seg = " + segment);
        }
        graph.addVertex(segment.from);
        graph.addVertex(segment.to);
        edge = new SegmentEdge(segment);
        graph.addEdge(segment.from, segment.to, edge);
        weight = getWeight(way, segment);
        System.out.println("edge for segment " + segment.id + "(from node "+ segment.from.id + " to node " 
          + segment.to.id + ") has weight: " + weight);
        graph.setEdgeWeight(edge, weight);
        if(!isOneWay(way)) {
          edge = new SegmentEdge(segment); // create a second edge for other direction
          graph.addEdge(segment.to, segment.from, edge);
          graph.setEdgeWeight(edge, weight);          
          System.out.println("inverse segment " + segment.id + "(from node "+ segment.to.id + " to node " 
            + segment.from.id + ") has weight: " + weight);
        }
      }
    }
    return graph;
  }
  
  /**
   * Returns the weight for the given segment depending on the highway type and the length of the 
   * segment.
   * @param way
   * @param segment
   * @return
   */
  public double getWeight(Way way, Segment segment) {
    String type = way.get("highway");
    if(type == null) {
      return 0.0d;
    }
    Double weightValue = highwayWeight.get(type);
    double weight;
    if(weightValue == null) {
      weight = DEFAULT_WEIGHT;
    } else {
      weight = weightValue.doubleValue();
    }
    double distance = Math.sqrt(segment.from.coor.distance(segment.to.coor)) * 111000; // deg to m (at equator :-)
    return distance; // weight;
  }
  
  public boolean isOneWay(Way way) {
    // FIXXME: oneway=-1 is ignored for the moment!
    return way.get("oneway") != null || "motorway".equals(way.get("highway"));
  }
  
  public Graph<Node, WayEdge> createGraph() {
    createEdges();
    DirectedWeightedMultigraph<Node, WayEdge> graph = new DirectedWeightedMultigraph<Node, WayEdge>(new JosmEdgeFactory());

    for(WayEdge edge : edges) {
      graph.addVertex(edge.getStartNode());
      graph.addVertex(edge.getEndNode());
      graph.addEdge(edge.getStartNode(), edge.getEndNode(), edge);
    }
    return graph;
  }
  
  private void createEdges() {
    System.out.println("Start free Memory: " + (Runtime.getRuntime().freeMemory()));
    // iterate all ways and segments for all nodes:
    for(Way way : Main.ds.ways) {
      for(Segment segment : way.segments) {
        addSegmentForNode(segment.from, segment);
        addSegmentForNode(segment.to, segment);
        segments.add(segment);
      }
      //ways.add(way);
    }
    System.out.println("after all segments free Memory: " + (Runtime.getRuntime().freeMemory()));
    // find nodes with one or more than two segments:
    Set<Segment> nodeSegments;
    for(Node node : nodeSegmentMap.keySet()) {
      nodeSegments = nodeSegmentMap.get(node);
      if(nodeSegments.size() == 1 || nodeSegments.size() > 2) {
        crossingNodes.add(node);
      }
    }
    System.out.println("after all crossings free Memory: " + (Runtime.getRuntime().freeMemory()));
    System.out.println("Number of Nodes: " + Main.ds.nodes.size());
    System.out.println("Number of Segments: " + Main.ds.segments.size());
    System.out.println("Number of Graph Vertices: " + crossingNodes.size());
    System.out.println("Number of Nodes in Segment Map: " + nodeSegmentMap.keySet().size());
    // find for every crossing node all connected crossing nodes:
    Node targetNode;
    WayEdge edge;
    List<Segment> edgeSegments;
    for(Node sourceNode : crossingNodes) {
      targetNode = sourceNode;
      for(Segment segment : nodeSegmentMap.get(sourceNode)) {
        edge = new WayEdge();
        edgeSegments = new ArrayList<Segment>();
        boolean crossingReached = false;
        do {
          targetNode = getOtherEnd(targetNode, segment);
          edgeSegments.add(segment);
          // FIXXME calculate length of segment for edge
          if(crossingNodes.contains(targetNode)) {
            crossingReached = true;
          } else {
            segment = getOtherSegment(targetNode, segment);
          }
        } while(!crossingReached);
        edge.setSegments(edgeSegments);
        edge.setStartNode(sourceNode);
        edge.setEndNode(targetNode);
        System.out.println("Adding edge with " + edgeSegments.size() +" segments: " + edgeSegments);
        System.out.println("after adding edge free Memory: " + (Runtime.getRuntime().freeMemory()));
        edges.add(edge);
      }
    }
  }
  
  /**
   * Returns the other segment for the given node (works only for non crossing 
   * nodes).
   * @param node
   * @param segment
   * @return
   */
  private Segment getOtherSegment(Node node, Segment segment) {
    Set<Segment> segments = nodeSegmentMap.get(node);
    if(segments.size() != 2) {
      throw new RuntimeException("the given node has more than two nodes!");
    }
    if(!segments.contains(segment)) {
      throw new RuntimeException("the given segment is not connected to the given node");
    }
    Iterator<Segment> segIter = segments.iterator(); 
    Segment next = segIter.next();
    if(segment.equals(next)) {
        return segIter.next();
    } else {
      return segment;
    }
  }
  
  private void addSegmentForNode(Node node, Segment segment) {
    Set<Segment> segments = nodeSegmentMap.get(node);
    if(segments == null) {
      segments = new HashSet<Segment>();
      nodeSegmentMap.put(node, segments);
    }
    if(!segments.contains(segment)) {
      segments.add(segment);
    }
  }
  
  private Node getOtherEnd(Node node, Segment segment) {
    if(segment.from.equals(node)) {
      return segment.to;
    } else {
      return segment.from;
    }
  }
  

}
