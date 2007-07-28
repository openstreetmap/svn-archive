/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @author cdaller
 *
 */
public class NavigatorModel {
    private Graph graph;
    private List<Node> nodes;
    private int selectionChangedCalls;
    List<Segment> segmentPath;
    List<SegmentEdge> edgePath;

    public NavigatorModel() {
        nodes = new ArrayList<Node>();
    }
    
    /**
     * @return the selectedNodes
     */
    public List<Node> getSelectedNodes() {
        return nodes;
    }


    public Graph<Node, SegmentEdge> getGraph() {
        if (graph == null) {
            OsmGraphCreator graphCreator = new OsmGraphCreator();
            // graph = graphCreator.createGraph();
            graph = graphCreator.createSegmentGraph();
        }
        return graph;
    }

    
    /**
     * 
     */
    public void calculateShortesPath() {
        System.out.print("navigate nodes ");
        for (Node node : nodes) {
            System.out.print(node.id + ",");
        }
        System.out.println();

        double fullWeight = 0;
        DijkstraShortestPath<Node, SegmentEdge> routing;
        List<SegmentEdge> fullPath = new ArrayList<SegmentEdge>();
        List<SegmentEdge> path;
        for (int index = 1; index < nodes.size(); ++index) {
            routing = new DijkstraShortestPath<Node, SegmentEdge>(getGraph(), nodes.get(index - 1), nodes.get(index));
            path = routing.getPathEdgeList();
            if (path == null) {
                System.out.println("no path found!");
                return;
            }
            fullPath.addAll(path);
            fullWeight += routing.getPathLength();
        }
        
        edgePath = new ArrayList<SegmentEdge>();
        edgePath.addAll(fullPath);
        
        System.out.println("shortest path found: " + fullPath + " weight: " + fullWeight);
        System.out.println(getPathDescription());
//        double weight2 = 0;
//        for(Segment seg : getSegmentPath()) {
//            weight2 += Math.sqrt(seg.from.coor.distance(seg.to.coor)) * 111000;
//        }
//        System.out.println("all added: " + weight2);
    }
    
    public String getPathDescription() {
        List<PathDescription> pathDescriptions = getPathDescriptions();
        
        // create text representation from description:
        StringBuilder builder = new StringBuilder();
        for(PathDescription desc : pathDescriptions) {
            builder.append("follow ");
            String tmp = desc.getWay().get("name");
            if(tmp == null) {
                builder.append("unkown street ");
            } else {
                builder.append(tmp).append(" ");
            }
            tmp = desc.getWay().get("highway");
            if(tmp != null) {
                builder.append("(").append(tmp).append(") ");
            }
            builder.append("for ").append((int)desc.getLength()).append(" meters, then\n");
        }
        builder.delete(builder.length() - ", then ".length(), builder.length());
        return builder.toString();
    }

    /**
     * @return
     */
    private List<PathDescription> getPathDescriptions() {
        List<PathDescription> pathDescriptions = new LinkedList<PathDescription>();
        PathDescription description;
        double length = 0;
        Way oldWay = null;
        Way way = null;
        for(SegmentEdge edge : edgePath) {
            way = edge.getWay();
            length += edge.getLengthInM();
            if(oldWay != null && !oldWay.equals(way)) {
                description = new PathDescription(oldWay, length);
                pathDescriptions.add(description);
                length = 0;
            }
            oldWay = way;
        }
        if(way != null) {
            description = new PathDescription(way, length);
            pathDescriptions.add(description);
        }
        return pathDescriptions;
    }


    /**
     * Resets the calculated graph.
     */
    public void resetGraph() {
        graph = null;
    }


    /**
     * @return the segmentPath or <code>null</code>.
     */
    public List<Segment> getSegmentPath() {
        if(segmentPath == null && edgePath != null) {
            segmentPath = new LinkedList<Segment>();
            synchronized(segmentPath) {
                for (SegmentEdge edge : edgePath) {
                    segmentPath.add(edge.getSegment());
                }
            }
        }
        return segmentPath;
    }


    /**
     * Adds a node to navigate.
     * @param node the node to add.
     */
    public void addNode(Node node) {
        nodes.add(node);
    }

    /**
     * @return the edgePath
     */
    public List<SegmentEdge> getEdgePath() {
        return this.edgePath;
    }
    
    /**
     * Resets all data (nodes, edges, segments).
     */
    public void reset() {
        segmentPath = null;
        edgePath = null;
        nodes.clear();
    }


}
