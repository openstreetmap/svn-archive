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
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Performs tests of a route at the level of the whole route: sorting test
 *
 * @author darya
 *
 */
public class RouteChecker extends Checker {

    private boolean hasGap;

    List<RelationMember> sortedMembers;

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

        if (hasGap(waysToCheck)) {

            this.hasGap = true;

            RelationSorter sorter = new RelationSorter();
            sortedMembers = sorter.sortMembers(waysToCheck);

            if (!hasGap(sortedMembers)) {
                Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_SORTING);
                builder.message(tr("PT: Route contains a gap that can be fixed by sorting"));
                builder.primitives(relation);
                TestError e = builder.build();
                this.errors.add(e);

            }

        }

    }

    /**
     * Checks if there is a gap for a given list of ways. It does not check if
     * the way actually stands for a public transport platform - that should be
     * checked beforehand.
     *
     * @param waysToCheck ways to check
     * @return true if has gap (in the sense of continuity of ways in the
     *         Relation Editor), false otherwise
     */
    private boolean hasGap(List<RelationMember> waysToCheck) {
        WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
        final List<WayConnectionType> links = connectionTypeCalculator.updateLinks(waysToCheck);
        for (int i = 0; i < links.size(); i++) {
            final WayConnectionType link = links.get(i);
            final boolean hasError = !(i == 0 || link.linkPrev) || !(i == links.size() - 1 || link.linkNext)
                    || link.direction == null || WayConnectionType.Direction.NONE.equals(link.direction);
            if (hasError) {
                return true;

            }
        }

        return false;
    }

    public List<RelationMember> getSortedMembers() {

        return sortedMembers;

    }

    public boolean getHasGap() {

        return this.hasGap;

    }

    protected static Command fixSortingError(TestError testError) {
        if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_SORTING) {
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
                if (member.getRole().equals("")) {
                    ways.add(member);
                } else {
                    RelationMember modifiedMember = new RelationMember("", member.getWay());
                    ways.add(modifiedMember);
                }

            } else { // stops:
                if (member.getRole().equals("stop_positon")) {
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

        ChangeCommand changeCommand = new ChangeCommand(originalRelation, sortedRelation);

        return changeCommand;

    }

}
