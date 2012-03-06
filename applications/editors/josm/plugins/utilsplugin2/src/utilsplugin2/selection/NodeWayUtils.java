// License: GPL. Copyright 2011 by Alexei Kasatkin
package utilsplugin2.selection;

import org.openstreetmap.josm.data.osm.Relation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Pair;

import static org.openstreetmap.josm.tools.I18n.tr;


/**
 * Class with some useful functions that are reused in extend selection actions
 *
 */
public final class NodeWayUtils {

    static final int maxLevel = Main.pref.getInteger("selection.maxrecursion", 15);
    static final int maxWays = Main.pref.getInteger("selection.maxfoundways", 2000);
    static final int maxWays1 = Main.pref.getInteger("selection.maxfoundways.intersection", 500);

    /**
     * Find the neighbours of node n on the way w and put them in given collection
     * @param w way on which the search goes
     * @param n node to find its neighbours
     * @param nodes collection to place the nodes we found
     */
    static void addNeighbours(Way w, Node n, Collection<Node> nodes) {
        List<Node> nodeList = w.getNodes();
        
        int idx = nodeList.indexOf(n);
        if (idx == -1) return;

        // add previous element
        if (idx > 0) {
            nodes.add(nodeList.get(idx - 1));
        }
        // add next element
        if (idx < nodeList.size() - 1) {
            nodes.add(nodeList.get(idx + 1));
        }
        if (w.isClosed()) {
            // cyclic neighbours detection
            if (idx == 0) {
                nodes.add(nodeList.get(nodeList.size() - 2));
            }
            if (idx == nodeList.size() - 1) {
                nodes.add(nodeList.get(1));
            }
        }
     }

    /**
     * Adds all ways attached to way to specified collection
     * @param w way to find attached ways
     * @param ways  collection to place the ways we found
     */
    static int addWaysConnectedToWay(Way w, Set<Way> ways) {
         int s = ways.size();
        List<Node> nodes = w.getNodes();
        boolean flag = ways.contains(w);
        for (Node n: nodes) {
            ways.addAll(OsmPrimitive.getFilteredList(n.getReferrers(), Way.class));
        }
        if (!flag) ways.remove(w);
        return ways.size() - s;
    }

    /**
     * Adds all ways attached to node to specified collection
     * @param n Node to find attached ways
     * @param ways  collection to place the ways we found
     */
    static int addWaysConnectedToNode(Node n, Set<Way> ways) {
        int s = ways.size();
        ways.addAll(OsmPrimitive.getFilteredList(n.getReferrers(), Way.class));
        return ways.size() - s;
    }

    /**
     * Adds all ways intersecting one way to specified set
     * @param ways collection of ways to search
     * @param w way to check intersections
     * @param newWays set to place the ways we found
     */
    static int addWaysIntersectingWay(Collection<Way> ways, Way w, Set<Way> newWays,Set<Way> excludeWays) {
        List<Pair<Node, Node>> nodePairs = w.getNodePairs(false);
        int count=0;
        for (Way anyway: ways) {
            if (anyway == w) continue;
            if (newWays.contains(anyway) || excludeWays.contains(anyway) ) continue;

            List<Pair<Node, Node>> nodePairs2 = anyway.getNodePairs(false);
            loop: for (Pair<Node,Node> p1 : nodePairs) {
                for (Pair<Node,Node> p2 : nodePairs2) {
                    if (null!=Geometry.getSegmentSegmentIntersection(
                            p1.a.getEastNorth(),p1.b.getEastNorth(),
                            p2.a.getEastNorth(),p2.b.getEastNorth())) {
                            newWays.add(anyway);
                            count++;
                            break loop;
                    }
                }
            }
        }
        return count;
    }


        static int addWaysIntersectingWay(Collection<Way> ways, Way w, Set<Way> newWays) {
        List<Pair<Node, Node>> nodePairs = w.getNodePairs(false);
        int count=0;
        for (Way anyway: ways) {
            if (anyway == w) continue;
            if (newWays.contains(anyway)) continue;
            List<Pair<Node, Node>> nodePairs2 = anyway.getNodePairs(false);
            loop: for (Pair<Node,Node> p1 : nodePairs) {
                for (Pair<Node,Node> p2 : nodePairs2) {
                    if (null!=Geometry.getSegmentSegmentIntersection(
                            p1.a.getEastNorth(),p1.b.getEastNorth(),
                            p2.a.getEastNorth(),p2.b.getEastNorth())) {
                            newWays.add(anyway);
                            count++;
                            break loop;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Adds all ways from allWays intersecting initWays way to specified set newWays
     * @param allWays collection of ways to search
     * @param initWays ways to check intersections
     * @param newWays set to place the ways we found
     */
    public static int addWaysIntersectingWays(Collection<Way> allWays, Collection<Way> initWays, Set<Way> newWays) {
        int count=0;
        for (Way w : initWays){
            count+=addWaysIntersectingWay(allWays, w, newWays);
        }
        return count;
    }
    
    public static void addWaysConnectedToWays(Collection<Way> ways, Set<Way> newWays) {
        for (Way w : ways){
            NodeWayUtils.addWaysConnectedToWay(w, newWays);
        }
    }

    public static int addWaysConnectedToNodes(Set<Node> selectedNodes, Set<Way> newWays) {
        int s = newWays.size();
        for (Node node: selectedNodes) {
            addWaysConnectedToNode(node, newWays);
        }
        return newWays.size() - s;
    }

    public static int addNodesConnectedToWays(Set<Way> initWays, Set<Node> newNodes) {
        int s = newNodes.size();
        for (Way w: initWays) {
                newNodes.addAll(w.getNodes());
        }
        return newNodes.size()-s;
    }

    public static void addWaysIntersectingWaysRecursively
            (Collection<Way> allWays, Collection<Way> initWays, Set<Way> newWays)
    {
            Set<Way> foundWays = new HashSet<Way>();
            foundWays.addAll(initWays);
            newWays.addAll(initWays);
            Set<Way> newFoundWays = new HashSet<Way>();

            int level=0,c;
            do {
                 c=0;
                 newFoundWays = new HashSet<Way>();
                 for (Way w : foundWays){
                      c+=addWaysIntersectingWay(allWays, w, newFoundWays,newWays);
                 }
                 foundWays = newFoundWays;
                 newWays.addAll(newFoundWays);
                 level++;
//                 System.out.printf("%d: %d ways added to selection intersectiong\n",level,c);
                 if (c>maxWays1) {
                       JOptionPane.showMessageDialog(Main.parent,
                                tr("Too many ways are added: {0}!",c),
                                        tr("Warning"),
                                        JOptionPane.WARNING_MESSAGE);
                       return;
                 }
            } while ( c >0 && level < maxLevel );
            return;
    }

    public static void addWaysConnectedToWaysRecursively
            (Collection<Way> initWays, Set<Way> newWays)
    {
            //long t = System.currentTimeMillis();
            int level=0,c;
            newWays.addAll(initWays);
            do {
                 c=0;
                 Set<Way> foundWays = new HashSet<Way>();
                 foundWays.addAll(newWays);
                 for (Way w : foundWays){
                      c+=addWaysConnectedToWay(w, newWays);
                 }
                 level++;
//                 System.out.printf("%d: %d ways added to selection\n",level,c);
                 if (c>maxWays) {
                       JOptionPane.showMessageDialog(Main.parent,
                                tr("Too many ways are added: {0}!",c),
                                        tr("Warning"),
                                        JOptionPane.WARNING_MESSAGE);
                       return;
                 }
            } while ( c >0 && level < maxLevel );
           // System.out.println("time = "+(System.currentTimeMillis()-t)+" ways = "+newWays.size());
            return;
    }

    static void addMiddle(Set<Node> selectedNodes, Set<Node> newNodes) {
        Iterator<Node> it=selectedNodes.iterator();
        Node n1 = it.next();
        Node n2 = it.next();
        Set<Way> ways=new HashSet<Way>();
        ways.addAll(OsmPrimitive.getFilteredList(n1.getReferrers(), Way.class));
        for (Way w: ways) {

            if (w.isUsable() && w.containsNode(n2) && w.containsNode(n1)) {
                // Way w goes from n1 to n2
                List <Node> nodes= w.getNodes();
                int i1 = nodes.indexOf(n1);
                int i2 = nodes.indexOf(n2);
                int n = nodes.size();
                if (i1>i2) { int p=i2; i2=i1; i1=p; } // now i1<i2
                if (w.isClosed()) {
                        if ((i2-i1)*2 <= n ) { // i1 ... i2
                            for (int i=i1+1;i!=i2; i++) {
                                newNodes.add(nodes.get(i));
                            }
                        } else { // i2 ... n-1 0 1 ... i1
                            for (int i=i2+1;i!=i1; i=(i+1)%n) {
                                newNodes.add(nodes.get(i));
                            }
                        }
                    } else {
                        for (int i=i1+1;i<i2;i++) {
                            newNodes.add(nodes.get(i));
                        }
                    }
            }
        }
        if (newNodes.size()==0) {
                JOptionPane.showMessageDialog(Main.parent,
                    tr("Please select two nodes connected by way!"),
                    tr("Warning"),
                    JOptionPane.WARNING_MESSAGE);
            }
    }
    
    static boolean addAreaBoundary(Way firstWay, Set<Way> newWays, boolean goingLeft) {
        Way w=firstWay;
        Node curNode = w.lastNode();
        Node prevNode = w.getNode(w.getNodes().size()-2);
        Set<Way> newestWays = new HashSet<Way>();
        while(true) {

            Node nextNode, endNode, otherEnd, preLast;
            Way nextWay;

            EastNorth en;
            double startHeading,bestAngle;

            en = curNode.getEastNorth();
            startHeading = prevNode.getEastNorth().heading( en );

            bestAngle = goingLeft ? -1e5 : 1e5 ;
            otherEnd=null; nextWay=null;
            
            for (OsmPrimitive ref : curNode.getReferrers()) {
                if (ref instanceof Way && ref!=w && ref.isSelectable()) {
                    //
                    Way w2 = (Way) ref;
                    //  -----[prevNode]-(curNode)-[nextNode]------[preLast]-(endNode)
                    //          w           |              w2
                    if (w2.getNodesCount()<2 || w2.isClosed()) continue;


                    if (curNode == w2.firstNode()) {
                        nextNode = w2.getNode(1);
                        preLast = w2.getNode(w2.getNodesCount()-2);
                        endNode = w2.lastNode();
                    } // forward direction
                    else if (curNode == w2.lastNode()) {
                        nextNode = w2.getNode(w2.getNodesCount()-2);
                        preLast = w2.getNode(1);
                        endNode = w2.firstNode();
                    } // backward direction
                        else continue; // we came to some way middle node

                    double angle = startHeading -Math.PI - en.heading(nextNode.getEastNorth());
                    while (angle<0) angle+=2*Math.PI;
                    
                    if (angle < bestAngle ^ goingLeft) {
                        bestAngle = angle;
                        otherEnd = endNode;
                        prevNode = preLast;
                        nextWay = w2;
                    }
                }
            }
            if (firstWay == nextWay ) {
                //we came to starting way, but not not the right end 
                if (otherEnd==firstWay.firstNode()) return false;
                newWays.addAll(newestWays);
                return true; // correct loop found 
            }
            if (newestWays.contains(nextWay)) {
                // P - like loop found
                return false;
            }
            if (nextWay != null) { 
                newestWays.add(nextWay);
                curNode = otherEnd;
                w = nextWay;
                // next way found, continuing
            } else {
                // no closed loop found
                return false;
            } 
        }
    }

    static void addAllInsideMultipolygon(DataSet data, Relation rel, Set<Way> newWays, Set<Node> newNodes) {
        if (!rel.isMultipolygon()) return;
        BBox box = rel.getBBox();
        Set<Way> usedWays = OsmPrimitive.getFilteredSet(rel.getMemberPrimitives(), Way.class);
        List<EastNorth> polyPoints = new ArrayList<EastNorth>(10000);
        
        for (Way way: usedWays) {
            List<Node> polyNodes = way.getNodes();
            // converts all points to EastNorth
            for (Node n: polyNodes) polyPoints.add(n.getEastNorth());  
        }
        
        
        List<Node> searchNodes = data.searchNodes(box);
        Set<Node> newestNodes = new HashSet<Node>();
        Set<Way> newestWays = new HashSet<Way>();
        for (Node n : searchNodes) {
            //if (Geometry.nodeInsidePolygon(n, polyNodes)) {
            if (NodeWayUtils.isPointInsidePolygon(n.getEastNorth(), polyPoints)>0) {
                newestNodes.add(n);
            }
        }
        
        List<Way> searchWays = data.searchWays(box);
        for (Way w : searchWays) {
            if (newestNodes.containsAll(w.getNodes())) {
                newestWays.add(w);
            }
        }
        for (Way w : newestWays) {
            newestNodes.removeAll(w.getNodes());
            // do not select nodes of already selected ways
        }
        
        newNodes.addAll(newestNodes);
        newWays.addAll(newestWays);
    }

    static void addAllInsideWay(DataSet data, Way way, Set<Way> newWays, Set<Node> newNodes) {
        if (!way.isClosed()) return;
        BBox box = way.getBBox();
        List<Node> polyNodes = way.getNodes();
        List<EastNorth> polyPoints = new ArrayList<EastNorth>(polyNodes.size());
        
        // converts all points to EastNorth
        for (Node n: polyNodes) polyPoints.add(n.getEastNorth());  
        
        List<Node> searchNodes = data.searchNodes(box);
        Set<Node> newestNodes = new HashSet<Node>();
        Set<Way> newestWays = new HashSet<Way>();
        for (Node n : searchNodes) {
            //if (Geometry.nodeInsidePolygon(n, polyNodes)) {
            if (NodeWayUtils.isPointInsidePolygon(n.getEastNorth(), polyPoints)>0) {
                newestNodes.add(n);
            }
        }
        
        List<Way> searchWays = data.searchWays(box);
        for (Way w : searchWays) {
            if (newestNodes.containsAll(w.getNodes())) {
                newestWays.add(w);
            }
        }
        for (Way w : newestWays) {
            newestNodes.removeAll(w.getNodes());
            // do not select nodes of already selected ways
        }
        
        newNodes.addAll(newestNodes);
        newWays.addAll(newestWays);
    }
    
    /**
     * @return 0 =  not inside polygon, 1 = strictly inside, 2 = near edge, 3 = near vertex
     */
    public static int isPointInsidePolygon(EastNorth point, List<EastNorth> polygonPoints) {
        int n=polygonPoints.size();
        EastNorth oldPoint = polygonPoints.get(n-1);
        double n1,n2,n3,e1,e2,e3,d;
        int interCount=0;
        
        for (EastNorth curPoint : polygonPoints) {
            n1 = curPoint.north(); n2 = oldPoint.north();  n3 =  point.north();
            e1 = curPoint.east(); e2 = oldPoint.east();  e3 =  point.east();
            
            if (Math.abs(n1-n3)<1e-5 && Math.abs(e1-e3)<1e-5) return 3; // vertex
            if (Math.abs(n2-n3)<1e-5 && Math.abs(e2-e3)<1e-5) return 3; // vertex
            
            // looking at oldPoint-curPoint segment
            if ( n1 > n2) {
                if (n1 > n3 && n3 >= n2) {
                    n1-=n3; n2-=n3; e1-=e3; e2-=e3;
                    d = e1*n2 - n1*e2;
                    if (d<-1e-5) {
                        interCount++; // there is OX intersecthion at e = (e1n2-e2n1)/(n2-n1) >=0
                    } else if (d<=1e-5) return 2; // boundary detected
                }
            } else if (n1 == n2) {
                if (n1 == n3) {
                    e1-=e3; e2-=e3;
                    if ((e1 <=0 && e2 >= 0) || (e1 >=0 && e2 <= 0)) return 2;// boundary detected
                }
            } else {
                if (n1 <= n3 && n3 < n2) {
                    n1-=n3; n2-=n3; e1-=e3; e2-=e3;
                    d = e1*n2 - n1*e2;
                    if (d>1e-5) {
                        interCount++; // there is OX intersecthion at e = (e1n2-e2n1)/(n2-n1) >=0
                    } else if (d>=-1e-5) return 2; // boundary detected
                }
            }
            oldPoint = curPoint;
        }
       // System.out.printf("Intersected intercount %d %s\n",interCount, point.toString());
        if (interCount%2 == 1) return 1; else return 0;
    }
    
    public static Collection<OsmPrimitive> selectAllInside(Collection<OsmPrimitive> selected, DataSet dataset) {
        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(selected, Way.class);
        Set<Relation> selectedRels = OsmPrimitive.getFilteredSet(selected, Relation.class);

        for (Iterator<Relation> it = selectedRels.iterator(); it.hasNext();) {
            Relation r = it.next();
            if (!r.isMultipolygon()) {
                it.remove();
            }
        }

        Set<Way> newWays = new HashSet<Way>();
        Set<Node> newNodes = new HashSet<Node>();
        // select ways attached to already selected ways
        if (!selectedWays.isEmpty()) {
            for (Way w: selectedWays) {
                addAllInsideWay(dataset,w,newWays,newNodes);
            }
        }
        if (!selectedRels.isEmpty()) {
            for (Relation r: selectedRels) {
                addAllInsideMultipolygon(dataset,r,newWays,newNodes);
            }
        }
        
        Set<OsmPrimitive> insideSelection = new HashSet<OsmPrimitive>();
        if (!newWays.isEmpty() || !newNodes.isEmpty()) {
            insideSelection.addAll(newWays);
            insideSelection.addAll(newNodes);
        }
        return insideSelection;
    }



}
