// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;

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

        int numOfGaps = countGaps(waysToCheck);

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
            }
        }
    }

    protected void performFromToTagsTest() {

        String from = relation.get("from");
        String to = relation.get("to");
        if (from == null || to == null || manager.getPTStopCount() == 0) {
            return;
        }

        PTStop stop = manager.getFirstStop();
        OsmPrimitive primitive = checkPTStopName(stop, from);

        if (primitive != null) {
            Builder builder = TestError.builder(this.test, Severity.WARNING,
                    PTAssistantValidatorTest.ERROR_CODE_FROM_TO_ROUTE_TAG);
            builder.message(tr("PT: The name of the first stop does not match the \"from\" tag of the route relation"));
            builder.primitives(primitive);
            TestError e = builder.build();
            this.errors.add(e);
        }

        stop = manager.getLastStop();
        primitive = checkPTStopName(stop, to);

        if (primitive != null) {
            Builder builder = TestError.builder(this.test, Severity.WARNING,
                    PTAssistantValidatorTest.ERROR_CODE_FROM_TO_ROUTE_TAG);
            builder.message(tr("PT: The name of the last stop does not match the \"to\" tag of the route relation"));
            builder.primitives(primitive);
            TestError e = builder.build();
            this.errors.add(e);
        }
    }

    //given a PTStop and a name, check whether one of its primitives have a
    //different name from the one passed. if so, it returns the primitive.
    //it returns null if the names match
    private OsmPrimitive checkPTStopName(PTStop stop, String name) {
        OsmPrimitive primitive = null;
        String toCheck = null;
        if (stop.getPlatform() != null) {
            toCheck = stop.getPlatform().getName();
            primitive = stop.getPlatform();
        }
        if (toCheck == null && stop.getStopPosition() != null) {
            toCheck = stop.getStopPosition().getName();
            primitive = stop.getStopPosition();
        }

        if (toCheck != null && !toCheck.equals(name))
            return primitive;

        return null;
    }

    public boolean hasGaps(List<RelationMember> waysToCheck) {
        return countGaps(waysToCheck) > 0;
    }

    /**
     * Counts how many gaps there are for a given list of ways. It does not check if
     * the way actually stands for a public transport platform - that should be
     * checked beforehand.
     *
     * @param waysToCheck ways to check
     * @return true if has gap (in the sense of continuity of ways in the
     *         Relation Editor), false otherwise
     */
    private int countGaps(List<RelationMember> waysToCheck) {
        int numberOfGaps = 0;

        WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
        final List<WayConnectionType> links = connectionTypeCalculator.updateLinks(waysToCheck);
        for (int i = 0; i < links.size(); i++) {
            final WayConnectionType link = links.get(i);
            if (!(i == 0 || link.linkPrev)
                    || !(i == links.size() - 1
                    || link.linkNext)
                    || link.direction == null
                    || WayConnectionType.Direction.NONE.equals(link.direction)) {
                numberOfGaps++;
                i++;
            }
        }

        return numberOfGaps;
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
