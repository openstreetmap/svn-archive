// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Performs tests of a route at the level of single ways: DirectionTest and
 * RoadTypeTest
 *
 * @author darya
 *
 */
public class WayChecker extends Checker {

    public WayChecker(Relation relation, Test test) {

        super(relation, test);

    }

    protected void performRoadTypeTest() {

        if (!relation.hasTag("route", "bus") && !relation.hasTag("route", "trolleybus")
                && !relation.hasTag("route", "share_taxi")) {
            return;
        }

        for (RelationMember rm : relation.getMembers()) {
            if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {

                Way way = rm.getWay();
                // at this point, the relation has already been checked to
                // be a route of public_transport:version 2

                boolean isCorrectRoadType = true;
                boolean isUnderConstruction = false;
                if (way.hasKey("construction")) {
                    isUnderConstruction = true;
                }
                if (relation.hasTag("route", "bus") || relation.hasTag("route", "share_taxi")) {
                    if (!RouteUtils.isWaySuitableForBuses(way)) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("highway", "construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "trolleybus")) {
                    if (!(RouteUtils.isWaySuitableForBuses(way) && way.hasTag("trolley_wire", "yes"))) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("highway", "construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "tram")) {
                    if (!way.hasTag("railway", "tram")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "subway")) {
                    if (!way.hasTag("railway", "subway")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "light_rail")) {
                    if (!way.hasTag("railway", "subway")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "light_rail")) {
                    if (!way.hasTag("railway", "light_rail")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction")) {
                        isUnderConstruction = true;
                    }
                } else if (relation.hasTag("route", "train")) {
                    if (!way.hasTag("railway", "rail")) {
                        isCorrectRoadType = false;
                    }
                    if (way.hasTag("railway", "construction")) {
                        isUnderConstruction = true;
                    }
                }

                if (!isCorrectRoadType && !isUnderConstruction) {

                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<Way> highlighted = new ArrayList<>(1);
                    highlighted.add(way);
                    Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_ROAD_TYPE);
                    builder.message(tr("PT: Route type does not match the type of the road it passes on"));
                    builder.primitives(primitives);
                    builder.highlight(highlighted);
                    TestError e = builder.build();
                    errors.add(e);

                }

                if (isUnderConstruction) {
                    List<Relation> primitives = new ArrayList<>(1);
                    primitives.add(relation);
                    List<Way> highlighted = new ArrayList<>(1);
                    highlighted.add(way);
                    Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_CONSTRUCTION);
                    builder.message(tr("PT: Road is under construction"));
                    builder.primitives(primitives);
                    builder.highlight(highlighted);
                    TestError e = builder.build();
                    errors.add(e);
                }
            }
        }

    }

    protected void performDirectionTest() {

        List<Way> waysToCheck = new ArrayList<>();

        for (RelationMember rm : relation.getMembers()) {
            if (RouteUtils.isPTWay(rm)) {
                if (rm.isWay()) {
                    waysToCheck.add(rm.getWay());
                } else {
                    Relation nestedRelation = rm.getRelation();
                    for (RelationMember nestedRelationMember : nestedRelation.getMembers()) {
                        waysToCheck.add(nestedRelationMember.getWay());
                    }
                }
            }
        }

        if (waysToCheck.size() <= 1) {
            return;
        }

        List<Way> problematicWays = new ArrayList<>();

        for (int i = 0; i < waysToCheck.size(); i++) {

            Way curr = waysToCheck.get(i);

            if (i == 0) {
                // first way:
                Way next = waysToCheck.get(i + 1);
                if (!touchCorrectly(null, curr, next)) {
                    problematicWays.add(curr);
                }

            } else if (i == waysToCheck.size() - 1) {
                // last way:
                Way prev = waysToCheck.get(i - 1);
                if (!touchCorrectly(prev, curr, null)) {
                    problematicWays.add(curr);
                }

            } else {
                // all other ways:
                Way prev = waysToCheck.get(i - 1);
                Way next = waysToCheck.get(i + 1);
                if (!touchCorrectly(prev, curr, next)) {
                    problematicWays.add(curr);
                }
            }
        }

        List<Relation> primitives = new ArrayList<>(1);
        primitives.add(this.relation);

        List<Set<Way>> listOfSets = new ArrayList<>();
        for (Way problematicWay : problematicWays) {
            Set<Way> primitivesToReport = new HashSet<>();
            primitivesToReport.add(problematicWay);
            primitivesToReport.addAll(checkAdjacentWays(problematicWay, new HashSet<Way>()));
            listOfSets.add(primitivesToReport);
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < listOfSets.size(); i++) {
                for (int j = i; j < listOfSets.size(); j++) {
                    if (i != j && RouteUtils.waysTouch(listOfSets.get(i), listOfSets.get(j))) {
                        listOfSets.get(i).addAll(listOfSets.get(j));
                        listOfSets.remove(j);
                        j = listOfSets.size();
                        changed = true;
                    }
                }
            }
        }

        for (Set<Way> currentSet : listOfSets) {
            Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_DIRECTION);
            builder.message(tr("PT: Route passes a oneway road in the wrong direction"));
            builder.primitives(primitives);
            builder.highlight(currentSet);
            TestError e = builder.build();
            this.errors.add(e);
        }

    }

    /**
     * Checks if the current way touches its neighboring ways correctly
     *
     * @param prev
     *            can be null
     * @param curr
     *            cannot be null
     * @param next
     *            can be null
     * @return {@code true} if the current way touches its neighboring ways correctly
     */
    private boolean touchCorrectly(Way prev, Way curr, Way next) {

        if (RouteUtils.isOnewayForPublicTransport(curr) == 0) {
            return true;
        }

        if (prev != null) {

            if (RouteUtils.waysTouch(curr, prev)) {
                Node nodeInQuestion;
                if (RouteUtils.isOnewayForPublicTransport(curr) == 1) {
                    nodeInQuestion = curr.firstNode();
                } else {
                    nodeInQuestion = curr.lastNode();
                }

                List<Way> nb = findNeighborWays(curr, nodeInQuestion);

                if (nb.size() < 2 && nodeInQuestion != prev.firstNode() && nodeInQuestion != prev.lastNode()) {
                    return false;
                }
            }
        }

        if (next != null) {

            if (RouteUtils.waysTouch(curr, next)) {
                Node nodeInQuestion;
                if (RouteUtils.isOnewayForPublicTransport(curr) == 1) {
                    nodeInQuestion = curr.lastNode();
                } else {
                    nodeInQuestion = curr.firstNode();
                }

                List<Way> nb = findNeighborWays(curr, nodeInQuestion);

                if (nb.size() < 2 && nodeInQuestion != next.firstNode() && nodeInQuestion != next.lastNode()) {
                    return false;
                }
            }
        }

        return true;

    }

    protected Set<Way> checkAdjacentWays(Way curr, Set<Way> flags) {
        // curr is supposed to be a wrong oneway way!!

        Set<Way> resultSet = new HashSet<>();
        resultSet.addAll(flags);
        resultSet.add(curr);

        if (RouteUtils.isOnewayForPublicTransport(curr) == 0) {
            return null;
        }

        Node firstNodeInRouteDirection;
        Node lastNodeInRouteDirection;
        if (RouteUtils.isOnewayForPublicTransport(curr) == 1) {
            firstNodeInRouteDirection = curr.lastNode();
            lastNodeInRouteDirection = curr.firstNode();
        } else {
            firstNodeInRouteDirection = curr.firstNode();
            lastNodeInRouteDirection = curr.lastNode();
        }

        List<Way> firstNodeInRouteDirectionNeighbors = findNeighborWays(curr, firstNodeInRouteDirection);
        List<Way> lastNodeInRouteDirectionNeighbors = findNeighborWays(curr, lastNodeInRouteDirection);

        for (Way nb : firstNodeInRouteDirectionNeighbors) {

            if (resultSet.contains(nb)) {
                continue;
            }

            if (RouteUtils.isOnewayForPublicTransport(nb) == 1 && nb.firstNode() == firstNodeInRouteDirection) {
                Set<Way> newSet = this.checkAdjacentWays(nb, resultSet);
                resultSet.addAll(newSet);

            } else if (RouteUtils.isOnewayForPublicTransport(nb) == -1 && nb.lastNode() == firstNodeInRouteDirection) {
                Set<Way> newSet = this.checkAdjacentWays(nb, resultSet);
                resultSet.addAll(newSet);

            }
        }

        for (Way nb : lastNodeInRouteDirectionNeighbors) {

            if (resultSet.contains(nb)) {
                continue;
            }

            if (RouteUtils.isOnewayForPublicTransport(nb) == 1 && nb.lastNode() == lastNodeInRouteDirection) {
                Set<Way> newSet = this.checkAdjacentWays(nb, resultSet);
                resultSet.addAll(newSet);
            } else if (RouteUtils.isOnewayForPublicTransport(nb) == -1 && nb.firstNode() == lastNodeInRouteDirection) {
                Set<Way> newSet = this.checkAdjacentWays(nb, resultSet);
                resultSet.addAll(newSet);
            }

        }

        return resultSet;

    }

    /**
     * Finds all ways that touch the given way at the given node AND belong to
     * the relation of this WayChecker
     *
     * @param way way
     * @param node node
     * @return all ways that touch the given way at the given node AND belong to
     * the relation of this WayChecker
     */
    private List<Way> findNeighborWays(Way way, Node node) {

        List<Way> resultList = new ArrayList<>();

        if (node != null) {
            List<OsmPrimitive> nodeReferrers = node.getReferrers();

            for (OsmPrimitive referrer : nodeReferrers) {
                if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
                    Way neighborWay = (Way) referrer;
                    if (neighborWay != way && containsWay(neighborWay)) {
                        resultList.add(neighborWay);
                    }
                }
            }
        }

        return resultList;
    }

    /**
     * Checks if the relation of this WayChecker contains the given way
     *
     * @param way way
     * @return {@code true} if the relation of this WayChecker contains the given way
     */
    private boolean containsWay(Way way) {

        List<RelationMember> members = relation.getMembers();

        for (RelationMember rm : members) {
            if (rm.isWay() && rm.getWay() == way) {
                return true;
            }
        }

        return false;

    }

    protected static Command fixErrorByRemovingWay(TestError testError) {

        if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_ROAD_TYPE
                && testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_DIRECTION) {
            return null;
        }

        Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
        Relation originalRelation = (Relation) primitives.iterator().next();
        Collection<?> highlighted = testError.getHighlighted();
        Way wayToRemove = (Way) highlighted.iterator().next();

        Relation modifiedRelation = new Relation(originalRelation);
        List<RelationMember> modifiedRelationMembers = new ArrayList<>(originalRelation.getMembersCount() - 1);

        // copy PT stops first, PT ways last:
        for (RelationMember rm : originalRelation.getMembers()) {
            if (RouteUtils.isPTStop(rm)) {

                if (rm.getRole().equals("stop_position")) {
                    if (rm.getType().equals(OsmPrimitiveType.NODE)) {
                        RelationMember newMember = new RelationMember("stop", rm.getNode());
                        modifiedRelationMembers.add(newMember);
                    } else { // if it is a way:
                        RelationMember newMember = new RelationMember("stop", rm.getWay());
                        modifiedRelationMembers.add(newMember);
                    }
                } else {
                    // if the relation member does not have the role
                    // "stop_position":
                    modifiedRelationMembers.add(rm);
                }

            }
        }

        // now copy PT ways:
        for (RelationMember rm : originalRelation.getMembers()) {
            if (RouteUtils.isPTWay(rm)) {
                Way wayToCheck = rm.getWay();
                if (wayToCheck != wayToRemove) {
                    if (rm.getRole().equals("forward") || rm.getRole().equals("backward")) {
                        RelationMember modifiedMember = new RelationMember("", wayToCheck);
                        modifiedRelationMembers.add(modifiedMember);
                    } else {
                        modifiedRelationMembers.add(rm);
                    }
                }
            }
        }

        modifiedRelation.setMembers(modifiedRelationMembers);

        ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);

        return changeCommand;
    }

}
