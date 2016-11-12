// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;

/**
 * Utils class for stop areas
 *
 * @author
 *
 */
public final class StopUtils {

    private StopUtils() {
        // private constructor for util classes
    }

    /**
     * Checks if a given relation is a stop_area.
     *
     * @param r
     *            Relation to be checked
     * @return true if the relation is a stop_area, false otherwise.
     */
    public static boolean isStopArea(Relation r) {

        if (r == null) {
            return false;
        }

        if (r.hasTag("public_transport", "stop_area")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a given object is a stop_position.
     *
     * @param r
     *            Relation to be checked
     * @return true if the object is a stop_position, false otherwise.
     */
    public static boolean verifyStopAreaStopPosition(OsmPrimitive rm) {

        if (rm == null) {
            return false;
        }

        if (rm.hasTag("public_transport", "stop_position")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a given object is a platform.
     *
     * @param r
     *            Relation to be checked
     * @return true if the object is a platform, false otherwise.
     */
    public static boolean verifyStopAreaPlatform(OsmPrimitive rm) {

        if (rm == null) {
            return false;
        }

        if (rm.hasTag("public_transport", "platform") || rm.hasTag("highway", "bus_stop")
                || rm.hasTag("highway", "platform") || rm.hasTag("railway", "platform")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a given object is part of an stop area relation
     *
     * @param r
     *            Object to be checked
     * @return true if the object part of stop area relation, false otherwise.
     */
    public static boolean verifyIfMemberOfStopArea(OsmPrimitive member) {

        for (Relation parentRelation : OsmPrimitive.getFilteredList(member.getReferrers(), Relation.class)) {
            if (StopUtils.isStopArea(parentRelation)) {
                return true;
            }
        }
        return false;
    }

}
