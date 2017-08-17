// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

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
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType.Direction;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;
import org.openstreetmap.josm.tools.Pair;

/**
 * Performs tests of a route at the level of the whole route: sorting test
 *
 * @author darya
 *
 */
public class RouteChecker extends Checker {

    private boolean hasGap;

    List<RelationMember> sortedMembers;

    /* Manager of the PTStops and PTWays of the current route */
    private PTRouteDataManager manager;

    /* Assigns PTStops to nearest PTWays and stores that correspondence */
    private StopToWayAssigner assigner;

    public RouteChecker(Relation relation, Test test) {

        super(relation, test);

        this.hasGap = false;

    }

    //checks if sorting the members of the current relation could make it
    //continuous or it could reduce the number of gaps. if one of the previous
    //is true raises a warning
    protected void performSortingTest() {

        final List<RelationMember> waysToCheck = new ArrayList<>();
        for (RelationMember rm : relation.getMembers()) {

            if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {
                waysToCheck.add(rm);
            }
        }

        if (waysToCheck.isEmpty()) {
            return;
        }

        List<Pair<Integer, Pair<Direction, Direction>>> gaps = getGaps(waysToCheck);
        int numOfGaps = gaps.size();

        if (numOfGaps > 0) {

            this.hasGap = true;

            RelationSorter sorter = new RelationSorter();
            sortedMembers = sorter.sortMembers(waysToCheck);

            int numOfGapsAfterSort = countGaps(sortedMembers);

            if (numOfGapsAfterSort == 0) {
                Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_SORTING);
                builder.message(tr("PT: Route contains a gap that can be fixed by sorting"));
                builder.primitives(relation);
                TestError e = builder.build();
                this.errors.add(e);
            } else if (numOfGapsAfterSort < numOfGaps) {
                Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_PARTIAL_SORTING);
                builder.message(tr("PT: Route gaps can decrease by sorting members. Further validations will be required"));
                builder.primitives(relation);
                TestError e = builder.build();
                this.errors.add(e);
            } else if (numOfGaps == numOfGapsAfterSort) {
                //if sorting doesn't help try to fix the gaps trivially
                //by adding one way
                for (Pair<Integer, Pair<Direction, Direction>> gap : gaps) {
                    Way before = waysToCheck.get(gap.a).getWay();
                    Way after = waysToCheck.get(gap.a + 1).getWay();

                    Way fix = findTrivialFix(before, gap.b.a, after, gap.b.b);
                    if (fix != null) {
                        Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_TRIVIAL_FIX);
                        builder.message(tr("PT: Route gap can be closed by adding a single way"));
                        builder.primitives(relation, before, fix, after);
                        TestError e = builder.build();
                        this.errors.add(e);
                    }
                }
            }
        }
    }

    //given two ways and the direction of the route on those two ways, it seeks
    //another way that connects the two given ones respecting the directions
    private Way findTrivialFix(Way before, Direction beforeDirection, Way after, Direction afterDirection) {
        Node startNode = beforeDirection == Direction.FORWARD ? before.lastNode() : before.firstNode();
        Node lastNode = afterDirection == Direction.FORWARD ? after.firstNode() : after.lastNode();
        List<Way> candidates = startNode.getParentWays();
        candidates.removeIf(w -> !RouteUtils.isWaySuitableForPublicTransport(w));

        for (Way candidate : candidates) {
            if (candidate.equals(before)) continue;
            if ((candidate.firstNode().equals(startNode) && candidate.lastNode().equals(lastNode))
                    || (candidate.lastNode().equals(startNode) && candidate.firstNode().equals(lastNode))) {
                return candidate;
            }
        }

        return null;
    }

    //checks if the from and to tags of the route match the names of the first
    //and last stops
    protected boolean performFromToTagsTest() {

        String from = relation.get("from");
        String to = relation.get("to");
        if (from == null || to == null || manager.getPTStopCount() == 0) {
            return false;
        }

        from = from.toLowerCase();
        to = to.toLowerCase();

        boolean foundError = false;
        PTStop stop = manager.getFirstStop();
        OsmPrimitive primitive = checkPTStopName(stop, from);

        if (primitive != null) {
            Builder builder = TestError.builder(this.test, Severity.WARNING,
                    PTAssistantValidatorTest.ERROR_CODE_FROM_TO_ROUTE_TAG);
            builder.message(tr("PT: The name of the first stop does not match the \"from\" tag of the route relation"));
            builder.primitives(primitive, relation);
            TestError e = builder.build();
            this.errors.add(e);
            foundError = true;
        }

        stop = manager.getLastStop();
        primitive = checkPTStopName(stop, to);

        if (primitive != null) {
            Builder builder = TestError.builder(this.test, Severity.WARNING,
                    PTAssistantValidatorTest.ERROR_CODE_FROM_TO_ROUTE_TAG);
            builder.message(tr("PT: The name of the last stop does not match the \"to\" tag of the route relation"));
            builder.primitives(primitive, relation);
            TestError e = builder.build();
            this.errors.add(e);
            foundError = true;
        }

        return foundError;
    }

    //checks if the first and last stop are assigned to the first and last way
    protected void performFirstLastWayStopTest() {

        if (manager.getPTStopCount() == 0 || manager.getPTWayCount() == 0) {
            return;
        }

        PTStop stop = manager.getFirstStop();
        Way way = manager.getFirstWay();
        if (!way.equals(assigner.get(stop))) {
            Builder builder = TestError.builder(this.test, Severity.WARNING,
                    PTAssistantValidatorTest.ERROR_CODE_FIRST_LAST_STOP_WAY_TAG);
            builder.message(tr("PT: The first stop of the route does not match the first way"));
            List<OsmPrimitive> prims = new ArrayList<>(Arrays.asList(way, relation));
            if (stop.getPlatform() != null)
                prims.add(stop.getPlatform());
            if (stop.getStopPosition() != null)
                prims.add(stop.getStopPosition());
            builder.primitives(prims);
            TestError e = builder.build();
            this.errors.add(e);
        }

        stop = manager.getLastStop();
        way = manager.getLastWay();
        if (!way.equals(assigner.get(stop))) {
            Builder builder = TestError.builder(this.test, Severity.WARNING,
                    PTAssistantValidatorTest.ERROR_CODE_FIRST_LAST_STOP_WAY_TAG);
            builder.message(tr("PT: The last stop of the route does not match the last way"));
            List<OsmPrimitive> prims = new ArrayList<>(Arrays.asList(way, relation));
            if (stop.getPlatform() != null)
                prims.add(stop.getPlatform());
            if (stop.getStopPosition() != null)
                prims.add(stop.getStopPosition());
            builder.primitives(prims);
            TestError e = builder.build();
            this.errors.add(e);
        }
    }

    //given a PTStop and a name, check whether one of its primitives have a
    //different name from the one passed. if so, it returns the primitive.
    //it compares not only the name tag but all the name:* names
    //it returns null if the names match
    private OsmPrimitive checkPTStopName(PTStop stop, String name) {
        OsmPrimitive primitive = null;
        List<String> toCheck = new ArrayList<>();
        if (stop.getPlatform() != null) {
            primitive = stop.getPlatform();
            toCheck.addAll(getPrimitiveNameTags(primitive));
        }
        if (toCheck.isEmpty() && stop.getStopPosition() != null) {
            primitive = stop.getStopPosition();
            toCheck.addAll(getPrimitiveNameTags(primitive));
        }

        for (String value : toCheck) {
            if (value.equals(name)) {
                return primitive;
            }
        }

        return null;
    }

    private List<String> getPrimitiveNameTags(OsmPrimitive primitive) {
        List<String> ret = new ArrayList<>();
        for (Entry<String, String> entry : primitive.getInterestingTags().entrySet()) {
            if ("name".equals(entry.getKey())
                    || entry.getKey().contains("name:")) {
                ret.add(entry.getValue().toLowerCase());
            }
        }
        return ret;
    }

    /**
     * Checks whether there is at least one gap in the given list of ways.
     *
     * @param waysToCheck ways to check
     * @return true if has gaps , false otherwise
     */
    public boolean hasGaps(List<RelationMember> waysToCheck) {
        return countGaps(waysToCheck) > 0;
    }

    /**
     * Counts how many gaps there are for a given list of ways.
     *
     * @param waysToCheck ways to check
     * @return number of gaps
     */
    private int countGaps(List<RelationMember> waysToCheck) {
        return getGaps(waysToCheck).size();
    }

    /**
     * Finds the gaps (in the sense of continuity of ways in the Relation
     * Editor) in a given list of ways. It does not check if the way actually
     * stands for a public transport platform - that should be checked beforehand.
     *
     * @param waysToCheck ways to check
     * @return It returns a list of gaps. a gap is a pair of an index (the index
     * of the way right before the gap) and a pair of directions of the two ways
     * around the gap.
     */
    private List<Pair<Integer, Pair<Direction, Direction>>> getGaps(List<RelationMember> waysToCheck) {

        WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
        final List<WayConnectionType> links = connectionTypeCalculator.updateLinks(waysToCheck);
        List<Pair<Integer, Pair<Direction, Direction>>> gaps = new ArrayList<>();

        for (int i = 0; i < links.size() -1; i++) {
            WayConnectionType link = links.get(i);
            if (!link.linkNext) {
                gaps.add(new Pair<>(i, new Pair<>(link.direction, links.get(i+1).direction)));
            }
        }

        return gaps;
    }

    public List<RelationMember> getSortedMembers() {

        return sortedMembers;

    }

    public boolean getHasGap() {

        return this.hasGap;

    }

    protected static Command fixSortingError(TestError testError) {
        if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_SORTING
                && testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_PARTIAL_SORTING) {
            return null;
        }

        Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
        Relation originalRelation = (Relation) primitives.iterator().next();

        // separate ways from stops (because otherwise the order of
        // stops/platforms can be messed up by the sorter:
        List<RelationMember> members = originalRelation.getMembers();
        final List<RelationMember> stops = new ArrayList<>();
        final List<RelationMember> ways = new ArrayList<>();
        for (RelationMember member : members) {
            if (RouteUtils.isPTWay(member)) {
                if ("".equals(member.getRole())) {
                    ways.add(member);
                } else {
                    RelationMember modifiedMember = new RelationMember("", member.getWay());
                    ways.add(modifiedMember);
                }
            } else { // stops:
                if ("stop_positon".equals(member.getRole())) {
                    // it is not expected that stop_positions could
                    // be relations
                    if (member.getType().equals(OsmPrimitiveType.NODE)) {
                        RelationMember modifiedMember = new RelationMember("stop", member.getNode());
                        stops.add(modifiedMember);
                    } else { // if it is a primitive of type way:
                        RelationMember modifiedMember = new RelationMember("stop", member.getWay());
                        stops.add(modifiedMember);
                    }
                } else { // if it is not a stop_position:
                    stops.add(member);
                }
            }
        }

        // sort the ways:
        RelationSorter sorter = new RelationSorter();
        List<RelationMember> sortedWays = sorter.sortMembers(ways);

        // create a new relation to pass to the command:
        Relation sortedRelation = new Relation(originalRelation);
        List<RelationMember> sortedRelationMembers = new ArrayList<>(members.size());
        for (RelationMember rm : stops) {
            sortedRelationMembers.add(rm);
        }
        for (RelationMember rm : sortedWays) {
            sortedRelationMembers.add(rm);
        }
        sortedRelation.setMembers(sortedRelationMembers);

        return new ChangeCommand(originalRelation, sortedRelation);
    }

    //the trivial fix simply adds the right way (the one found during the
    //detection phase) in the gap in order to close it
    protected static Command fixTrivialError(TestError testError) {

        if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_TRIVIAL_FIX) {
            return null;
        }

        List<OsmPrimitive> primitives = new ArrayList<>(testError.getPrimitives());
        Relation originalRelation = (Relation) primitives.get(0);
        Way before = (Way) primitives.get(1);
        Way fix = (Way) primitives.get(2);

        int index = 0;
        List<RelationMember> members = originalRelation.getMembers();
        for (index = 0; index < members.size(); index++) {
            if (members.get(index).getMember().equals(before)) {
                break;
            }
        }

        Relation fixedRelation = new Relation(originalRelation);
        fixedRelation.addMember(index + 1, new RelationMember(null, fix));

        return new ChangeCommand(originalRelation, fixedRelation);
    }

    public PTRouteDataManager getManager() {
        return manager;
    }

    public void setManager(PTRouteDataManager manager) {
        this.manager = manager;
    }

    public StopToWayAssigner getAssigner() {
        return assigner;
    }

    public void setAssigner(StopToWayAssigner assigner) {
        this.assigner = assigner;
    }
}
