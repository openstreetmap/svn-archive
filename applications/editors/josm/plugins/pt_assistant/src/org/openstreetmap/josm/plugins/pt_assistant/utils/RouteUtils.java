package org.openstreetmap.josm.plugins.pt_assistant.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
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
			return true;
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

		return !isPTWay(rm);

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

		if (rm.hasRole("") && OsmPrimitiveType.WAY.equals(rm.getType())) {
			Way way = rm.getWay();
			if (!way.hasTag("public_transport", "platform") && !way.hasTag("highway", "platform")
					&& !way.hasTag("railway", "platform")) {
				return true;
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

		return false;
	}


}
