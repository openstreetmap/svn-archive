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

/**
 * @author cdaller
 *
 */
public class NavigatorModel {
    private Graph graph;
    private List<Node> nodes;
    private int selectionChangedCalls;
    List<Segment> segmentPath;

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
        segmentPath = new LinkedList<Segment>();
        synchronized(segmentPath) {
            for (SegmentEdge edge : fullPath) {
                segmentPath.add(edge.getSegment());
            }
        }
        Main.ds.setSelected(segmentPath);
        Main.map.mapView.repaint();
        System.out.println("shortest path found: " + fullPath + " weight: " + fullWeight);
        
        double weight2 = 0;
        for(Segment seg : segmentPath) {
            weight2 += Math.sqrt(seg.from.coor.distance(seg.to.coor)) * 111000;
        }
        System.out.println("all added: " + weight2);
    }


    /**
     * Resets the calculated graph.
     */
    public void resetGraph() {
        graph = null;
    }


    /**
     * @return the segmentPath
     */
    public List<Segment> getSegmentPath() {
        return this.segmentPath;
    }


    /**
     * Adds a node to navigate.
     * @param node the node to add.
     */
    public void addNode(Node node) {
        nodes.add(node);
    }
    
    /**
     * Clear all nodes to navigate. 
     */
    public void clearNodes() {
        nodes.clear();        
    }


}
