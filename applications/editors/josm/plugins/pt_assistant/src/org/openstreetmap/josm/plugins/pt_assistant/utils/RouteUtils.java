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
 * @author darya, giacomo, polyglot
 *
 */
public final class RouteUtils {

    private static final String PT_VERSION_TAG = "public_transport:version";
    public static final String TAG_ROUTE = "route";
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
    public static boolean isVersionTwoPTRoute(Relation r) {

        if (!isPTRoute(r)) {
            return false;
        }

        if (!r.hasTag(PT_VERSION_TAG, "2")) {
            return false;
        }

        return !r.hasTag("bus", "on_demand");
    }

    public static boolean isVersionOnePTRoute(Relation r) {

        if (!isPTRoute(r)) {
            return false;
        }

        if (r.get(PT_VERSION_TAG) == null) {
            return true;
        }

        return r.hasTag(PT_VERSION_TAG, "1");
    }

    public static boolean isPTRoute(Relation r) {

        if (r == null) {
            return false;
        }

        String[] acceptedRouteTags = new String[] {
                "bus", "trolleybus", "share_taxi",
                "tram", "light_rail", "subway", "train"};

        return r.hasTag(TAG_ROUTE, acceptedRouteTags);
    }

    public static boolean isRoute(Relation r) {
        return r.get(TAG_ROUTE) != null;
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
            return !(rm.getWay().hasTag("public_transport", "platform")
                    || rm.getWay().hasTag("highway", "platform")
                    || rm.getWay().hasTag("railway", "platform"));
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

        if (OsmUtils.isTrue(way.get("oneway"))
                || OsmUtils.isReversed(way.get("oneway"))
                || way.hasTag("junction", "roundabout")
                || way.hasTag("highway", "motorway")) {

            if (!way.hasTag("busway", "lane")
                    && !way.hasTag("busway", "opposite_lane")
                    && !way.hasTag("busway:left", "lane")
                    && !way.hasTag("busway:right", "lane")
                    && !way.hasTag("oneway:bus", "no")
                    && !way.hasTag("oneway:psv", "no")
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
     * Checks if the given way has tags that make it oneway for bicycles
     * The test does not check whether the way violates those
     * restrictions.
     *
     * @return 0 if the way is not oneway for bicycles, 1 if the way is
     *         oneway for bicycles, -1 if the way is reversely oneway
     *         for bicycles
     */
    public static int isOnewayForBicycles(Way way) {

        if (OsmUtils.isTrue(way.get("oneway"))
                || OsmUtils.isReversed(way.get("oneway"))
                || way.hasTag("junction", "roundabout")) {

            if (!way.hasTag("busway", "lane")
                    && !way.hasTag("cycleway", "opposite_lane")
                    && !way.hasTag("cycleway:left", "lane")
                    && !way.hasTag("cycleway:right", "lane")
                    && !way.hasTag("oneway:bicycle", "no")) {

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

        return w1FirstNode == w2FirstNode
                || w1FirstNode == w2LastNode
                || w1LastNode == w2FirstNode
                || w1LastNode == w2LastNode;
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

        String[] acceptedHighwayTags = new String[] {
                "motorway", "trunk", "primary", "secondary", "tertiary",
                "unclassified", "road", "residential", "service",
                "motorway_link", "trunk_link", "primary_link", "secondary_link",
                "tertiary_link", "living_street", "bus_guideway", "road"};

        if (way.hasTag("highway", acceptedHighwayTags)
                || way.hasTag("cycleway", "share_busway")
                || way.hasTag("cycleway", "shared_lane")) {
            return true;
        }

        return (way.hasTag("highway", "pedestrian")
                && (way.hasTag("bus", "yes", "designated")
                    || way.hasTag("psv", "yes", "designated")));
    }

    /**
     * Checks if this way is suitable for public transport (not only for buses)
     * @param way way
     * @return {@code true} if this way is suitable for public transport
     */
    public static boolean isWaySuitableForPublicTransport(Way way) {

        String[] acceptedRailwayTags = new String[] {
                "tram", "subway", "light_rail", "rail"};

        return isWaySuitableForBuses(way)
                || way.hasTag("railway", acceptedRailwayTags);
    }

    public static boolean isBicycleRoute(Relation r) {
        if (r == null) {
            return false;
        }

        return r.hasTag(TAG_ROUTE, "bicycle", "mtb");
    }


    /**
     * Checks if this way is suitable for bicycles
     * @param way way
     * @return {@code true} if this way is suitable for bicycles
     */
    public static boolean isWaySuitableForBicycle(Way way) {

        return way.hasTag("highway", "cycleway")
                || !(way.hasKey("highway", "motorway")
                  || way.hasKey("bicycle", "no")
                  || way.hasKey("bicycle", "use_sidepath"));
    }

    public static boolean isFootRoute(Relation r) {
        if (r == null) {
            return false;
        }

        return r.hasTag(TAG_ROUTE, "foot", "walking", "hiking");
    }

    public static boolean isHorseRoute(Relation r) {
        if (r == null) {
            return false;
        }

        return r.hasTag(TAG_ROUTE, "horse");
    }
}
