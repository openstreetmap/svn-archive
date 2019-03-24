// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Pair;

/**
 * Class with some useful functions that are reused in extend selection actions
 *
 */
public final class NodeWayUtils {

    static final int maxLevel = Config.getPref().getInt("selection.maxrecursion", 15);
    static final int maxWays = Config.getPref().getInt("selection.maxfoundways", 2000);
    static final int maxWays1 = Config.getPref().getInt("selection.maxfoundways.intersection", 500);

    private NodeWayUtils() {
        // Hide default constructor for utilities classes
    }

    private static <T extends OsmPrimitive> void filteredAdd(Collection<T> collection, T element) {
        if (!element.isDisabled()) {
            collection.add(element);
        }
    }

    private static <T extends OsmPrimitive> void filteredAddAll(Collection<T> out, Collection<T> in) {
        for (T element: in) {
            filteredAdd(out, element);
        }
    }

    /**
     * Find the neighbours of node n on the way w and put them in given collection
     * @param w way on which the search goes
     * @param n node to find its neighbours
     * @param nodes collection to place the nodes we found
     */
    static void addNeighbours(Way w, Node n, Collection<Node> nodes) {
        if (!n.getParentWays().contains(w))
            return;

        List<Node> nodeList = w.getNodes();
        int idx = nodeList.indexOf(n);
        if (idx == -1) return;

        // add previous element
        if (idx > 0) {
            filteredAdd(nodes, nodeList.get(idx - 1));
        }
        // add next element
        if (idx < nodeList.size() - 1) {
            filteredAdd(nodes, nodeList.get(idx + 1));
        }
        if (w.isClosed()) {
            // cyclic neighbours detection
            if (idx == 0) {
                filteredAdd(nodes, nodeList.get(nodeList.size() - 2));
            }
            if (idx == nodeList.size() - 1) {
                filteredAdd(nodes, nodeList.get(1));
            }
        }
    }

    /**
     * Adds all ways attached to way to specified collection
     * @param w way to find attached ways
     * @param ways  collection to place the ways we found
     * @return number of ways added
     */
    static int addWaysConnectedToWay(Way w, Set<Way> ways) {
        int s = ways.size();
        List<Node> nodes = w.getNodes();
        boolean flag = ways.contains(w);
        for (Node n: nodes) {
            filteredAddAll(ways, n.getParentWays());
        }
        if (!flag) ways.remove(w);
        return ways.size() - s;
    }

    /**
     * Adds all ways attached to node to specified collection
     * @param n Node to find attached ways
     * @param ways  collection to place the ways we found
     * @return number of ways added
     */
    static int addWaysConnectedToNode(Node n, Set<Way> ways) {
        int s = ways.size();
        filteredAddAll(ways, n.getParentWays());
        return ways.size() - s;
    }

    /**
     * Adds all ways intersecting one way to specified set
     * @param ways collection of ways to search
     * @param w way to check intersections
     * @param newWays set to place the ways we found
     * @param excludeWays set of excluded ways
     * @return number of ways possibly added added to newWays
     */
    static int addWaysIntersectingWay(Collection<Way> ways, Way w, Set<Way> newWays, Set<Way> excludeWays) {
        List<Pair<Node, Node>> nodePairs = w.getNodePairs(false);
        int count = 0;
        for (Way anyway: ways) {
            if (anyway.isDisabled()) continue;
            if (Objects.equals(anyway, w)) continue;
            if (newWays.contains(anyway) || excludeWays.contains(anyway)) continue;

            List<Pair<Node, Node>> nodePairs2 = anyway.getNodePairs(false);
            loop: for (Pair<Node, Node> p1 : nodePairs) {
                for (Pair<Node, Node> p2 : nodePairs2) {
                    if (null != Geometry.getSegmentSegmentIntersection(
                            p1.a.getEastNorth(), p1.b.getEastNorth(),
                            p2.a.getEastNorth(), p2.b.getEastNorth())) {
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
        Set<Way> excludeWays = new HashSet<Way>();
        return addWaysIntersectingWay(ways, w, newWays, excludeWays);
    }

    /**
     * Adds all ways from allWays intersecting initWays way to specified set newWays
     * @param allWays collection of ways to search
     * @param initWays ways to check intersections
     * @param newWays set to place the ways we found
     * @return number of ways added to newWays
     */
    public static int addWaysIntersectingWays(Collection<Way> allWays, Collection<Way> initWays, Set<Way> newWays) {
        // performance improvement - filter everything ahead of time
        Set<Way> filteredWays = new HashSet<>();
        filteredAddAll(filteredWays, allWays);
        int count = 0;
        for (Way w : initWays) {
            count += addWaysIntersectingWay(filteredWays, w, newWays);
        }
        return count;
    }

    public static void addWaysConnectedToWays(Collection<Way> ways, Set<Way> newWays) {
        for (Way w : ways) {
            NodeWayUtils.addWaysConnectedToWay(w, newWays);
        }
    }

    public static int addWaysConnectedToNodes(Collection<Node> selectedNodes, Set<Way> newWays) {
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

    public static void addWaysIntersectingWaysRecursively(Collection<Way> allWays, Collection<Way> initWays, Set<Way> newWays) {
        Set<Way> foundWays = new HashSet<>();
        foundWays.addAll(initWays);
        newWays.addAll(initWays);
        Set<Way> newFoundWays;
        // performance improvement - apply filters ahead of time
        Set<Way> filteredWays = new HashSet<>();
        filteredAddAll(filteredWays, allWays);
        filteredWays.removeAll(initWays);

        int level = 0, c;
        do {
            c = 0;
            newFoundWays = new HashSet<>();
            for (Way w : foundWays) {
                c += addWaysIntersectingWay(filteredWays, w, newFoundWays);
            }
            foundWays = newFoundWays;
            newWays.addAll(newFoundWays);
            level++;
            if (c > maxWays1) {
                new Notification(
                        tr("Too many ways are added: {0}!", c)
                        ).setIcon(JOptionPane.WARNING_MESSAGE).show();
                return;
            }
            if (level >= maxLevel) {
                new Notification(
                        tr("Reached max recursion depth: {0}", level)
                        ).setIcon(JOptionPane.WARNING_MESSAGE).show();
                return;
            }
        } while (c > 0);
    }

    public static void addWaysConnectedToWaysRecursively(Collection<Way> initWays, Set<Way> newWays) {
        int level = 0, c;
        newWays.addAll(initWays);
        do {
            c = 0;
            Set<Way> foundWays = new HashSet<>();
            foundWays.addAll(newWays);
            for (Way w : foundWays) {
                c += addWaysConnectedToWay(w, newWays);
            }
            level++;
            if (c > maxWays) {
                new Notification(
                        tr("Too many ways are added: {0}!", c)
                        ).setIcon(JOptionPane.WARNING_MESSAGE).show();
                return;
            }
            if (level >= maxLevel) {
                new Notification(
                        tr("Reached max recursion depth: {0}", level)
                        ).setIcon(JOptionPane.WARNING_MESSAGE).show();
                return;
            }
        } while (c > 0);
    }

    static void addMiddle(Set<Node> selectedNodes, Set<Node> newNodes) {
        Iterator<Node> it = selectedNodes.iterator();
        Node n1 = it.next();
        Node n2 = it.next();
        Set<Way> ways = new HashSet<>();
        ways.addAll(n1.getParentWays());
        for (Way w: ways) {

            if (w.isUsable() && w.containsNode(n2) && w.containsNode(n1)) {
                // Way w goes from n1 to n2
                List<Node> nodes = w.getNodes();
                int i1 = nodes.indexOf(n1);
                int i2 = nodes.indexOf(n2);
                int n = nodes.size();
                if (i1 > i2) {
                    int p = i2; i2 = i1; i1 = p; // now i1<i2
                }
                if (w.isClosed()) {
                    if ((i2-i1)*2 <= n) { // i1 ... i2
                        for (int i = i1+1; i != i2; i++) {
                            filteredAdd(newNodes, nodes.get(i));
                        }
                    } else { // i2 ... n-1 0 1 ... i1
                        for (int i = i2+1; i != i1; i = (i+1) % n) {
                            filteredAdd(newNodes, nodes.get(i));
                        }
                    }
                } else {
                    for (int i = i1+1; i < i2; i++) {
                        filteredAdd(newNodes, nodes.get(i));
                    }
                }
            }
        }
        if (newNodes.isEmpty()) {
            new Notification(
                    tr("Please select two nodes connected by way!")
                    ).setIcon(JOptionPane.WARNING_MESSAGE).show();
        }
    }

    static boolean addAreaBoundary(Way firstWay, Set<Way> newWays, boolean goingLeft) {
        Way w = firstWay;
        Node curNode = w.lastNode();
        Node prevNode = w.getNode(w.getNodes().size()-2);
        Set<Way> newestWays = new HashSet<>();
        while (true) {

            Node nextNode, endNode, otherEnd, preLast;
            Way nextWay;

            EastNorth en;
            double startHeading, bestAngle;

            en = curNode.getEastNorth();
            startHeading = prevNode.getEastNorth().heading(en);

            bestAngle = goingLeft ? -1e5 : 1e5;
            otherEnd = null;
            nextWay = null;

            for (OsmPrimitive ref : curNode.getReferrers()) {
                if (ref instanceof Way && !Objects.equals(ref, w) && ref.isSelectable()) {
                    //
                    Way w2 = (Way) ref;
                    //  -----[prevNode]-(curNode)-[nextNode]------[preLast]-(endNode)
                    //          w           |              w2
                    if (w2.getNodesCount() < 2 || w2.isClosed()) continue;

                    if (Objects.equals(curNode, w2.firstNode())) {
                        nextNode = w2.getNode(1);
                        preLast = w2.getNode(w2.getNodesCount()-2);
                        endNode = w2.lastNode(); // forward direction
                    } else if (Objects.equals(curNode, w2.lastNode())) {
                        nextNode = w2.getNode(w2.getNodesCount()-2);
                        preLast = w2.getNode(1);
                        endNode = w2.firstNode(); // backward direction
                    } else continue; // we came to some way middle node

                    double angle = startHeading -Math.PI - en.heading(nextNode.getEastNorth());
                    while (angle < 0) {
                        angle += 2*Math.PI;
                    }

                    if (angle < bestAngle ^ goingLeft) {
                        bestAngle = angle;
                        otherEnd = endNode;
                        prevNode = preLast;
                        nextWay = w2;
                    }
                }
            }
            if (Objects.equals(firstWay, nextWay)) {
                //we came to starting way, but not not the right end
                if (Objects.equals(otherEnd, firstWay.firstNode())) return false;
                filteredAddAll(newWays, newestWays);
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
        Collection<Way> usedWays = rel.getMemberPrimitives(Way.class);
        List<EastNorth> polyPoints = buildPointList(usedWays);

        List<Node> searchNodes = data.searchNodes(box);
        Set<Node> newestNodes = new HashSet<>();
        Set<Way> newestWays = new HashSet<>();
        for (Node n : searchNodes) {
            //if (Geometry.nodeInsidePolygon(n, polyNodes)) {
            if (NodeWayUtils.isPointInsidePolygon(n.getEastNorth(), polyPoints)) {
                // can't filter nodes here, would prevent selecting ways that have filtered nodes
                newestNodes.add(n);
            }
        }

        List<Way> searchWays = data.searchWays(box);
        for (Way w : searchWays) {
            if (newestNodes.containsAll(w.getNodes())) {
                filteredAdd(newestWays, w);
            }
        }
        for (Way w : newestWays) {
            newestNodes.removeAll(w.getNodes());
            // do not select nodes of already selected ways
        }

        filteredAddAll(newNodes, newestNodes);
        newWays.addAll(newestWays); // already filtered
    }

    static void addAllInsideWay(DataSet data, Way way, Set<Way> newWays, Set<Node> newNodes) {
        if (!way.isClosed()) return;
        BBox box = way.getBBox();
        Iterable<EastNorth> polyPoints = getWayPoints(way);

        List<Node> searchNodes = data.searchNodes(box);
        Set<Node> newestNodes = new HashSet<>();
        Set<Way> newestWays = new HashSet<>();
        for (Node n : searchNodes) {
            //if (Geometry.nodeInsidePolygon(n, polyNodes)) {
            if (NodeWayUtils.isPointInsidePolygon(n.getEastNorth(), polyPoints)) {
                // can't filter nodes here, would prevent selecting ways that have filtered nodes
                newestNodes.add(n);
            }
        }

        List<Way> searchWays = data.searchWays(box);
        for (Way w : searchWays) {
            if (newestNodes.containsAll(w.getNodes())) {
                filteredAdd(newestWays, w);
            }
        }

        filteredAddAll(newNodes, newestNodes);
        newWays.addAll(newestWays); // already filtered
    }

    public static boolean isPointInsidePolygon(EastNorth point, Iterable<EastNorth> polygonPoints) {
        int n = getRayIntersectionsCount(point, polygonPoints);
        if (n < 0) return true; // we are near node or near edge
        return (n % 2 != 0);
    }

    /**
     * @param point - point to start an OX-parallel  ray
     * @param polygonPoints - poits forming bundary, use null to split unconnected segmants
     * @return 0 =  not inside polygon, 1 = strictly inside, 2 = near edge, 3 = near vertex
     */
    public static int getRayIntersectionsCount(EastNorth point, Iterable<EastNorth> polygonPoints) {
        if (point == null) return 0;
        EastNorth oldPoint = null;
        double n1, n2, n3, e1, e2, e3, d;
        int interCount = 0;

        for (EastNorth curPoint : polygonPoints) {
            if (oldPoint == null || curPoint == null) {
                oldPoint = curPoint;
                continue;
            }
            n1 = curPoint.north(); n2 = oldPoint.north(); n3 = point.north();
            e1 = curPoint.east(); e2 = oldPoint.east(); e3 = point.east();

            if (Math.abs(n1-n3) < 1e-5 && Math.abs(e1-e3) < 1e-5) return -3; // vertex
            if (Math.abs(n2-n3) < 1e-5 && Math.abs(e2-e3) < 1e-5) return -3; // vertex

            // looking at oldPoint-curPoint segment
            if (n1 > n2) {
                if (n1 > n3 && n3 >= n2) {
                    n1 -= n3; n2 -= n3; e1 -= e3; e2 -= e3;
                    d = e1*n2 - n1*e2;
                    if (d < -1e-5) {
                        interCount++; // there is OX intersecthion at e = (e1n2-e2n1)/(n2-n1) >= 0
                    } else if (d <= 1e-5) return -2; // boundary detected
                }
            } else if (n1 == n2) {
                if (n1 == n3) {
                    e1 -= e3; e2 -= e3;
                    if ((e1 <= 0 && e2 >= 0) || (e1 >= 0 && e2 <= 0)) return -2; // boundary detected
                }
            } else {
                if (n1 <= n3 && n3 < n2) {
                    n1 -= n3; n2 -= n3; e1 -= e3; e2 -= e3;
                    d = e1*n2 - n1*e2;
                    if (d > 1e-5) {
                        interCount++; // there is OX intersecthion at e = (e1n2-e2n1)/(n2-n1) >= 0
                    } else if (d >= -1e-5) return -2; // boundary detected
                }
            }
            oldPoint = curPoint;
        }
        // System.out.printf("Intersected intercount %d %s\n",interCount, point.toString());
        return interCount;
    }

    public static Collection<OsmPrimitive> selectAllInside(Collection<OsmPrimitive> selected, DataSet dataset) {
        return selectAllInside(selected, dataset, true);
    }

    public static Set<OsmPrimitive> selectAllInside(Collection<OsmPrimitive> selected, DataSet dataset, boolean ignoreNodesOfFoundWays) {
        Set<Way> newWays = new HashSet<>();
        Set<Node> newNodes = new HashSet<>();
        // select nodes and ways inside selected ways and multipolygons
        for (OsmPrimitive p: selected) {
            if (p instanceof Way) {
                addAllInsideWay(dataset, (Way) p, newWays, newNodes);
            }
        }
        for (OsmPrimitive p: selected) {
            if ((p instanceof Relation) && p.isMultipolygon()) {
                addAllInsideMultipolygon(dataset, (Relation) p, newWays, newNodes);
            }
        }
        if (ignoreNodesOfFoundWays) {
            for (Way w : newWays) {
                newNodes.removeAll(w.getNodes());
                // do not select nodes of already selected ways
            }
        }

        Set<OsmPrimitive> insideSelection = new HashSet<>();
        if (!newWays.isEmpty() || !newNodes.isEmpty()) {
            insideSelection.addAll(newWays);
            insideSelection.addAll(newNodes);
        }
        return insideSelection;
    }

    private static List<EastNorth> buildPointList(Iterable<Way> ways) {
        ArrayList<EastNorth> points = new ArrayList<>(1000);
        for (Way way: ways) {
            for (EastNorth en: getWayPoints(way)) {
                points.add(en);
            }
            points.add(null); // next segment indicator
        }
        return points;
    }

    public static Iterable<EastNorth> getWayPoints(final Way w) {
        return new Iterable<EastNorth>() {
            @Override
            public Iterator<EastNorth> iterator() {
                return new Iterator<EastNorth>() {
                    int idx = 0;
                    @Override public boolean hasNext() {
                        return idx < w.getNodesCount();
                    }

                    @Override public EastNorth next() {
                        return w.getNode(idx++).getEastNorth();
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
