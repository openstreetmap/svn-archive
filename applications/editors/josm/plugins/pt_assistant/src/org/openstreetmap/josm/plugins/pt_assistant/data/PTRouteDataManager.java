// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Creates a representation of a route relation in the pt_assistant data model,
 * then maintains a list of PTStops and PTWays of a route.
 *
 * @author darya
 *
 */
public class PTRouteDataManager {

    /* The route relation */
    Relation relation;

    /* Stores all relation members that are PTStops */
    private List<PTStop> ptstops = new ArrayList<>();

    /* Stores all relation members that are PTWays */
    private List<PTWay> ptways = new ArrayList<>();

    /*
     * Stores relation members that could not be created because they are not
     * expected in the model for public_transport version 2
     */
    private Set<RelationMember> failedMembers = new HashSet<>();

    public PTRouteDataManager(Relation relation) throws IllegalArgumentException {

        // It is assumed that the relation is a route. Build in a check here
        // (e.g. from class RouteUtils) if you want to invoke this constructor
        // from outside the pt_assitant SegmentChecker)

        this.relation = relation;

        PTStop prev = null; // stores the last created PTStop

        for (RelationMember member : this.relation.getMembers()) {

            if (RouteUtils.isPTStop(member)) {

                // First, check if the stop already exists (i.e. there are
                // consecutive elements that belong to the same stop:
                boolean stopExists = false;

                if (prev != null) {
                    if (prev.getName() == null || prev.getName().equals("") || member.getMember().get("name") == null
                            || member.getMember().get("name").equals("")) {

                        // if there is no name, check by proximity:
                        // Squared distance of 0.000004 corresponds to
                        // around 100 m
                        if (this.calculateDistanceSq(member, prev) < 0.000001) {
                            stopExists = true;
                        }

                    } else {

                        // if there is a name, check by name comparison:
                        if (prev.getName().equalsIgnoreCase(member.getMember().get("name"))) {

                            stopExists = true;
                        }
                    }
                }

                // check if there are consecutive elements that belong to
                // the same stop:
                if (stopExists) {
                    // this PTStop already exists, so just add a new
                    // element:
                    prev.addStopElement(member);
                    // TODO: something may need to be done if adding the
                    // element
                    // did not succeed. The failure is a result of the same
                    // stop
                    // having >1 stop_position, platform or stop_area.
                } else {
                    // this PTStop does not exist yet, so create it:

                    try {
                        PTStop ptstop = new PTStop(member);
                        ptstops.add(ptstop);
                        prev = ptstop;
                    } catch (IllegalArgumentException ex) {
                        if (ex.getMessage().equals(
                                "The RelationMember type does not match its role " + member.getMember().getName())) {
                            if (!failedMembers.contains(member)) {
                                failedMembers.add(member);
                            }
                        } else {
                            throw ex;
                        }
                    }

                }

            } else if (RouteUtils.isPTWay(member)) {

                PTWay ptway = new PTWay(member);
                ptways.add(ptway);

            } else {
                if (!failedMembers.contains(member)) {
                    failedMembers.add(member);
                }

            }

        }
    }

    /**
     * Calculates the squared distance between the centers of bounding boxes of
     * two relation members (which are supposed to be platforms or
     * stop_positions)
     *
     * @param member1 first member
     * @param member2 second member
     * @return Squared distance between the centers of the bounding boxes of the
     *         given relation members
     */
    private double calculateDistanceSq(RelationMember member1, RelationMember member2) {
        LatLon coord1 = member1.getMember().getBBox().getCenter();
        LatLon coord2 = member2.getMember().getBBox().getCenter();
        return coord1.distanceSq(coord2);
    }

    /**
     * Assigns the given way to a PTWay of this route relation. If multiple
     * PTWays contain the same inputWay, the first found PTWay is returned.
     *
     * @param inputWay
     *            Way to be assigned to a PTWAy of this route relation
     * @return PTWay that contains the geometry of the inputWay, null if not
     *         found
     */
    public PTWay getPTWay(Way inputWay) {

        for (PTWay curr : ptways) {

            if (curr.isWay() && curr.getWays().get(0) == inputWay) {
                return curr;
            }

            if (curr.isRelation()) {
                for (RelationMember rm : curr.getRelation().getMembers()) {
                    Way wayInNestedRelation = rm.getWay();
                    if (wayInNestedRelation == inputWay) {
                        return curr;
                    }
                }
            }
        }

        return null; // if not found
    }

    public List<PTStop> getPTStops() {
        return this.ptstops;
    }

    public List<PTWay> getPTWays() {
        return this.ptways;
    }

    public int getPTStopCount() {
        return ptstops.size();
    }

    public int getPTWayCount() {
        return this.ptways.size();
    }

    public PTStop getFirstStop() {
        if (this.ptstops.isEmpty()) {
            return null;
        }
        return this.ptstops.get(0);
    }

    public PTStop getLastStop() {
        if (this.ptstops.isEmpty()) {
            return null;
        }
        return this.ptstops.get(ptstops.size() - 1);
    }

    public Set<RelationMember> getFailedMembers() {
        return this.failedMembers;
    }

    /**
     * Returns the route relation for which this manager was created:
     *
     * @return the route relation for which this manager was created
     */
    public Relation getRelation() {
        return this.relation;
    }

    /**
     * Returns a PTStop that matches the given id. Returns null if not found
     *
     * @param id identifier
     * @return a PTStop that matches the given id. Returns null if not found
     */
    public PTStop getPTStop(long id) {
        for (PTStop stop : this.ptstops) {
            if (stop.getStopPosition() != null && stop.getStopPosition().getId() == id) {
                return stop;
            }

            if (stop.getPlatform() != null && stop.getPlatform().getId() == id) {
                return stop;
            }
        }

        return null;
    }

    /**
     * Returns a PTWay that matches the given id. Returns null if not found
     *
     * @param id identifier
     * @return a PTWay that matches the given id. Returns null if not found
     */
    public PTWay getPTWay(long id) {
        for (PTWay ptway : this.ptways) {
            for (Way way : ptway.getWays()) {
                if (way.getId() == id) {
                    return ptway;
                }
            }
        }
        return null;
    }

    /**
     * Returns all PTWays of this route that contain the given way.
     *
     * @param way way
     * @return all PTWays of this route that contain the given way
     */
    public List<PTWay> findPTWaysThatContain(Way way) {

        List<PTWay> ptwaysThatContain = new ArrayList<>();
        for (PTWay ptway : ptways) {
            if (ptway.getWays().contains(way)) {
                ptwaysThatContain.add(ptway);
            }
        }
        return ptwaysThatContain;
    }

    /**
     * Returns all PTWays of this route that contain the given node as their
     * first or last node.
     *
     * @return all PTWays of this route that contain the given node as their
     * first or last node
     */
    public List<PTWay> findPTWaysThatContainAsEndNode(Node node) {

        List<PTWay> ptwaysThatContain = new ArrayList<>();
        for (PTWay ptway : ptways) {
            List<Way> ways = ptway.getWays();
            if (ways.get(0).firstNode() == node || ways.get(0).lastNode() == node
                    || ways.get(ways.size() - 1).firstNode() == node || ways.get(ways.size() - 1).lastNode() == node) {
                ptwaysThatContain.add(ptway);
            }
        }
        return ptwaysThatContain;

    }

    /**
     * Checks if at most one PTWay of this route refers to the given node
     *
     * @param node node
     * @return {@code true} if at most one PTWay of this route refers to the given node
     */
    public boolean isDeadendNode(Node node) {

        List<PTWay> referringPtways = this.findPTWaysThatContainAsEndNode(node);
        if (referringPtways.size() <= 1) {
            return true;
        }
        return false;
    }

    /**
     * Returns the PTWay which comes directly after the given ptway according to
     * the existing route member sorting
     *
     * @param ptway way
     * @return the PTWay which comes directly after the given ptway according to
     * the existing route member sorting
     */
    public PTWay getNextPTWay(PTWay ptway) {

        for (int i = 0; i < ptways.size() - 1; i++) {
            if (ptways.get(i) == ptway) {
                return ptways.get(i + 1);
            }
        }
        return null;

    }

    /**
     * Returns the PTWay which comes directly before the given ptway according
     * to the existing route member sorting
     *
     * @param ptway way
     * @return the PTWay which comes directly before the given ptway according
     * to the existing route member sorting
     */
    public PTWay getPreviousPTWay(PTWay ptway) {

        for (int i = 1; i < ptways.size(); i++) {
            if (ptways.get(i) == ptway) {
                return ptways.get(i - 1);
            }
        }
        return null;
    }

    /**
     * Returns a sequence of PTWays that are between the start way and the end
     * way. The resulting list includes the start and end PTWays.
     *
     * @param start start way
     * @param end end way
     * @return a sequence of PTWays that are between the start way and the end way
     */
    public List<PTWay> getPTWaysBetween(Way start, Way end) {

        List<Integer> potentialStartIndices = new ArrayList<>();
        List<Integer> potentialEndIndices = new ArrayList<>();

        for (int i = 0; i < ptways.size(); i++) {
            if (ptways.get(i).getWays().contains(start)) {
                potentialStartIndices.add(i);
            }
            if (ptways.get(i).getWays().contains(end)) {
                potentialEndIndices.add(i);
            }
        }

        List<int[]> pairList = new ArrayList<>();
        for (Integer potentialStartIndex : potentialStartIndices) {
            for (Integer potentialEndIndex : potentialEndIndices) {
                if (potentialStartIndex <= potentialEndIndex) {
                    int[] pair = {potentialStartIndex, potentialEndIndex};
                    pairList.add(pair);
                }
            }
        }

        int minDifference = Integer.MAX_VALUE;
        int[] mostSuitablePair = {0, 0};
        for (int[] pair : pairList) {
            int diff = pair[1] - pair[0];
            if (diff < minDifference) {
                minDifference = diff;
                mostSuitablePair = pair;
            }
        }

        List<PTWay> result = new ArrayList<>();
        for (int i = mostSuitablePair[0]; i <= mostSuitablePair[1]; i++) {
            result.add(ptways.get(i));
        }
        return result;
    }

    /**
     * Returns the common Node of two PTWays or null if there is no common Node.
     * If there is more than one common Node, only the first found is returned.
     *
     * @param way1 first way
     * @param way2 second way
     * @return the common Node of two PTWays or null if there is no common Node
     */
    public Node getCommonNode(PTWay way1, PTWay way2) {

        List<Way> wayList1 = way1.getWays();
        List<Way> wayList2 = way2.getWays();

        HashSet<Node> nodeSet1 = new HashSet<>();
        for (Way w : wayList1) {
            nodeSet1.addAll(w.getNodes());
        }
        HashSet<Node> nodeSet2 = new HashSet<>();
        for (Way w : wayList2) {
            nodeSet2.addAll(w.getNodes());
        }

        for (Node n : nodeSet1) {
            if (nodeSet2.contains(n)) {
                return n;
            }
        }

        return null;
    }

    /**
     * Returns the last way of this route
     *
     * @return the last way of this route
     */
    public Way getLastWay() {
        PTWay lastPTWay = this.ptways.get(ptways.size() - 1);
        if (lastPTWay == null) {
            return null;
        }
        return lastPTWay.getWays().get(lastPTWay.getWays().size() - 1);
    }

}
