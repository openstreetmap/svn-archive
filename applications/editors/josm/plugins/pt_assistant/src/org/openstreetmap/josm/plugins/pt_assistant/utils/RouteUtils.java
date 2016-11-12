// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.utils;

import java.util.Collection;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.OsmUtils;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Utils class for routes
 *
 * @author darya
 *
 */
public final class RouteUtils {

    private RouteUtils() {
        // private constructor for util classes
    }

    /**
     * Checks if the relation is a route of one of the following categories:
     * bus, trolleybus, share_taxi, tram, light_rail, subway, train.
     *
     * @param r
     *            Relation to be checked
     * @return true if the route belongs to the categories that can be validated
     *         with the pt_assistant plugin, false otherwise.
     */
    public static boolean isTwoDirectionRoute(Relation r) {

        if (r == null) {
            return false;
        }

        if (!r.hasKey("route") || !r.hasTag("public_transport:version", "2")) {
            return false;
        }
        if (r.hasTag("route", "bus") || r.hasTag("route", "trolleybus") || r.hasTag("route", "share_taxi")
                || r.hasTag("route", "tram") || r.hasTag("route", "light_rail") || r.hasTag("route", "subway")
                || r.hasTag("route", "train")) {

            if (!r.hasTag("bus", "on_demand")) {
                return true;
            }

        }
        return false;
    }

    /**
     * Checks if the relation member refers to a stop in a public transport
     * route. Some stops can be modeled with ways.
     *
     * @param rm
     *            relation member to be checked
     * @return true if the relation member refers to a stop, false otherwise
     */
    public static boolean isPTStop(RelationMember rm) {


        if (rm.getType().equals(OsmPrimitiveType.NODE)) {
                return true;
        }

        if (rm.getType().equals(OsmPrimitiveType.WAY)) {
            if (rm.getWay().hasTag("public_transport", "platform") || rm.getWay().hasTag("highway", "platform")
                    || rm.getWay().hasTag("railway", "platform")) {
                return true;
            }
        }

        return false;

    }

    /**
     * Checks if the relation member refers to a way in a public transport
     * route. Some OsmPrimitiveType.WAY have to be excluded because platforms
     * can be modeled with ways.
     *
     * @param rm
     *            relation member to be checked
     * @return true if the relation member refers to a way in a public transport
     *         route, false otherwise.
     */
    public static boolean isPTWay(RelationMember rm) {

        if (rm.getType().equals(OsmPrimitiveType.NODE)) {
            return false;
        }

        if (rm.getType().equals(OsmPrimitiveType.WAY)) {
            if (rm.getWay().hasTag("public_transport", "platform") || rm.getWay().hasTag("highway", "platform")
                    || rm.getWay().hasTag("railway", "platform")) {
                return false;
            }
            return true;
        }

        Relation nestedRelation = rm.getRelation();

        for (RelationMember nestedRelationMember : nestedRelation.getMembers()) {
            if (!nestedRelationMember.getType().equals(OsmPrimitiveType.WAY)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given way has tags that make it oneway for public
     * transport. The test does not check whether the way violates those
     * restrictions.
     *
     * @return 0 if the way is not oneway for public transport, 1 if the way is
     *         oneway for public transport, -1 if the way is reversely oneway
     *         for public transport
     */
    public static int isOnewayForPublicTransport(Way way) {

        if (OsmUtils.isTrue(way.get("oneway")) || OsmUtils.isReversed(way.get("oneway"))
                || way.hasTag("junction", "roundabout") || way.hasTag("highway", "motorway")) {

            if (!way.hasTag("busway", "lane") && !way.hasTag("busway:left", "lane")
                    && !way.hasTag("busway:right", "lane") && !way.hasTag("oneway:bus", "no")
                    && !way.hasTag("busway", "opposite_lane") && !way.hasTag("oneway:psv", "no")
                    && !way.hasTag("trolley_wire", "backward")) {

                if (OsmUtils.isReversed(way.get("oneway"))) {
                    return -1;
                }

                return 1;

            }

        }

        return 0;
    }

    /**
     * Checks if the ways have a common node
     *
     * @param w1 first way
     * @param w2 second way
     * @return {@code true} if the ways have a common node
     */
    public static boolean waysTouch(Way w1, Way w2) {

        if (w1 == null || w2 == null) {
            return false;
        }

        Node w1FirstNode = w1.firstNode();
        Node w1LastNode = w1.lastNode();
        Node w2FirstNode = w2.firstNode();
        Node w2LastNode = w2.lastNode();

        if (w1FirstNode == w2FirstNode || w1FirstNode == w2LastNode || w1LastNode == w2FirstNode
                || w1LastNode == w2LastNode) {
            return true;
        }

        return false;
    }

    /**
     * Checks if any way from the first collection touches any way from the
     * second collection
     *
     * @param c1 first collection
     * @param c2 second collection
     * @return true if ways touch, false otherwise
     */
    public static boolean waysTouch(Collection<Way> c1, Collection<Way> c2) {

        if (c1 == null || c2 == null) {
            return false;
        }

        for (Way w1 : c1) {
            for (Way w2 : c2) {
                if (waysTouch(w1, w2)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the type of the way is suitable for buses to go on it. The
     * direction of the way (i.e. one-way roads) is irrelevant for this test.
     *
     * @param way
     *            to be checked
     * @return true if the way is suitable for buses, false otherwise.
     */
    public static boolean isWaySuitableForBuses(Way way) {
        if (way.hasTag("highway", "motorway") || way.hasTag("highway", "trunk") || way.hasTag("highway", "primary")
                || way.hasTag("highway", "secondary") || way.hasTag("highway", "tertiary")
                || way.hasTag("highway", "unclassified") || way.hasTag("highway", "road")
                || way.hasTag("highway", "residential") || way.hasTag("highway", "service")
                || way.hasTag("highway", "motorway_link") || way.hasTag("highway", "trunk_link")
                || way.hasTag("highway", "primary_link") || way.hasTag("highway", "secondary_link")
                || way.hasTag("highway", "tertiary_link") || way.hasTag("highway", "living_street")
                || way.hasTag("highway", "bus_guideway") || way.hasTag("highway", "road")
                || way.hasTag("cycleway", "share_busway") || way.hasTag("cycleway", "shared_lane")) {
            return true;
        }

        if (way.hasTag("highway", "pedestrian") && (way.hasTag("bus", "yes") || way.hasTag("psv", "yes")
                || way.hasTag("bus", "designated") || way.hasTag("psv", "designated"))) {
            return true;
        }

        return false;
    }

    /**
     * Checks if this way is suitable for public transport (not only for buses)
     * @param way way
     * @return {@code true} if this way is suitable for public transport
     */
    public static boolean isWaySuitableForPublicTransport(Way way) {

        if (isWaySuitableForBuses(way) || way.hasTag("railway", "tram") || way.hasTag("railway", "subway")
                || way.hasTag("railway", "subway") || way.hasTag("railway", "light_rail")
                || way.hasTag("railway", "rail")) {
            return true;
        }

        return false;

    }

}
