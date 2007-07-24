/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;

/**
 * Plugin that allows navigation in josm
 * 
 * @author cdaller
 *
 */
public class NavigatorPlugin {
  private Graph graph;

  
  /**
   * 
   */
  public NavigatorPlugin() {
    super();
    JMenuBar menu = Main.main.menu;
    JMenu navigatorMenu = new JMenu(tr("Navigation"));
    JMenuItem navigatorMenuItem = new JMenuItem(new NavigatorAction(this));
    navigatorMenu.add(navigatorMenuItem);
    JMenuItem resetMenuItem = new JMenuItem(tr("Reset Graph"));
    resetMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        graph = null;        
      }      
    });
    navigatorMenu.add(resetMenuItem);
    menu.add(navigatorMenu);
    
  }
  
  public Graph<Node, SegmentEdge> getGraph() {
    if(graph == null) {
      OsmGraphCreator graphCreator = new OsmGraphCreator();
      //graph = graphCreator.createGraph();
      graph = graphCreator.createSegmentGraph();
    }
    return graph;
  }

  /**
   * @param startNode
   * @param endNode
   */
  public void navigate(List<Node> nodes) {
    System.out.print("navigate nodes ");
    for(Node node : nodes) {
      System.out.print(node.id + ",");
    }
    System.out.println();
    
    DijkstraShortestPath<Node, SegmentEdge> routing;
    List<SegmentEdge> fullPath = new ArrayList<SegmentEdge>();
    List<SegmentEdge> path;
    for(int index = 1; index < nodes.size(); ++index) {
      routing = new DijkstraShortestPath<Node, SegmentEdge>(getGraph(), nodes.get(index - 1), nodes.get(index));
      path = routing.getPathEdgeList();
      if(path == null) {
        System.out.println("no path found!");
        return;
      }
      fullPath.addAll(path);
    }
    List<Segment> segmentPath = new ArrayList<Segment>();
    for(SegmentEdge edge : fullPath) {
      segmentPath.add(edge.getSegment());
    }
    Main.ds.setSelected(segmentPath);
    Main.map.mapView.repaint();
    System.out.println("shortest path found: " + fullPath);
  }
}
