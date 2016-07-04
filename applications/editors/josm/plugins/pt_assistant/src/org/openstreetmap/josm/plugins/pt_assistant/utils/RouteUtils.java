package org.openstreetmap.josm.plugins.pt_assistant.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;

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

		if (rm.hasRole("stop") || rm.hasRole("stop_entry_only") || rm.hasRole("stop_exit_only")
				|| rm.hasRole("platform") || rm.hasRole("platform_entry_only") || rm.hasRole("platform_exit_only")) {

			if (rm.getType().equals(OsmPrimitiveType.NODE)) {

				if (rm.getNode().hasTag("public_transport", "stop_position")
						|| rm.getNode().hasTag("highway", "bus_stop")
						|| rm.getNode().hasTag("public_transport", "platform")
						|| rm.getNode().hasTag("highway", "platform") || rm.getNode().hasTag("railway", "platform")) {
					return true;

				}
			}

			if (rm.getType().equals(OsmPrimitiveType.WAY)) {
				if (rm.getWay().hasTag("public_transport", "platform") || rm.getWay().hasTag("highway", "platform")
						|| rm.getWay().hasTag("railway", "platform")) {
					return true;
				}
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

}
