// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopUtils;

/**
 * Performs tests of the stop area relations
 *
 * @author
 *
 */
public class StopChecker extends Checker {

    Set<OsmPrimitive> members;

    protected StopChecker(Relation relation, Test test) {
        super(relation, test);

        this.members = relation.getMemberPrimitives();
    }

    /**
     * Checks if the given stop area relation has a stop position.
     */
    protected void performStopAreaStopPositionTest() {

        // No errors if there is a member tagged as stop position.
        for (OsmPrimitive member : members) {
            if (StopUtils.verifyStopAreaStopPosition(member)) {
                return;
            }
        }

        // Throw error message
        List<OsmPrimitive> primitives = new ArrayList<>(1);
        primitives.add(relation);
        Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_NO_STOPS);
        builder.message(tr("PT: Stop area relation has no stop position"));
        builder.primitives(primitives);
        TestError e = builder.build();
        errors.add(e);
    }

    /**
     * Checks if the given stop area relation has a platform.
     */
    protected void performStopAreaPlatformTest() {

        // No errors if there is a member tagged as platform.
        for (OsmPrimitive member : members) {
            if (StopUtils.verifyStopAreaPlatform(member)) {
                return;
            }
        }

        // Throw error message
        List<OsmPrimitive> primitives = new ArrayList<>(1);
        primitives.add(relation);
        Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_NO_PLATFORM);
        builder.message(tr("PT: Stop area relation has no platform"));
        builder.primitives(primitives);
        TestError e = builder.build();
        errors.add(e);

    }

    /**
     * Checks if the stop_position(s) of an stop area belong to the same route
     * relations as its related platform(s).
     */
    protected void performStopAreaRelationsTest() {

        HashMap<Long, Long> stopPositionRelationIds = new HashMap<>();
        HashMap<Long, Long> platformRelationIds = new HashMap<>();

        // Loop through all members
        for (OsmPrimitive member : members) {

            // For stop positions...
            if (StopUtils.verifyStopAreaStopPosition(member)) {

                // Create a list of assigned route relations
                for (Relation referrer : OsmPrimitive.getFilteredList(member.getReferrers(), Relation.class)) {
                    if (referrer.get("type") == "route") {
                        stopPositionRelationIds.put(referrer.getId(), referrer.getId());
                    }
                }
            // For platforms...
            } else if (StopUtils.verifyStopAreaPlatform(member)) {

                // Create a list of assigned route relations
                for (Relation referrer : OsmPrimitive.getFilteredList(member.getReferrers(), Relation.class)) {
                    if (referrer.get("type") == "route") {
                        platformRelationIds.put(referrer.getId(), referrer.getId());
                    }
                }
            }
        }

        // Check if the stop_position has no referrers at all. If it has no
        // referrers, then no error should be reported (changed on 11.08.2016 by
        // darya):
        if (stopPositionRelationIds.isEmpty()) {
            return;
        }

        // Check if route relation lists are identical
        if (stopPositionRelationIds.equals(platformRelationIds)) {
            return;
        }

        // Throw error message
        List<OsmPrimitive> primitives = new ArrayList<>(1);
        primitives.add(relation);
        Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_COMPARE_RELATIONS);
        builder.message(tr("PT: Route relations of stop position(s) and platform(s) of stop area members diverge"));
        builder.primitives(primitives);
        TestError e = builder.build();
        errors.add(e);
    }

}
