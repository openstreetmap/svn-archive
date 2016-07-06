package org.openstreetmap.josm.plugins.pt_assistant.utils;

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
public class RouteUtils {

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

		// if (rm.hasRole("stop") || rm.hasRole("stop_entry_only") ||
		// rm.hasRole("stop_exit_only")
		// || rm.hasRole("platform") || rm.hasRole("platform_entry_only") ||
		// rm.hasRole("platform_exit_only")) {

		if (rm.getType().equals(OsmPrimitiveType.NODE)) {

			if (rm.getNode().hasTag("public_transport", "stop_position") || rm.getNode().hasTag("highway", "bus_stop")
					|| rm.getNode().hasTag("public_transport", "platform") || rm.getNode().hasTag("highway", "platform")
					|| rm.getNode().hasTag("railway", "platform")) {
				return true;

			}
		}

		if (rm.getType().equals(OsmPrimitiveType.WAY)) {
			if (rm.getWay().hasTag("public_transport", "platform") || rm.getWay().hasTag("highway", "platform")
					|| rm.getWay().hasTag("railway", "platform")) {
				return true;
			}
		}
		// }

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

}
