package org.openstreetmap.josm.plugins.pt_assistant.utils;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.DownloadPrimitiveAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
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

		if (rm.getType().equals(OsmPrimitiveType.NODE)) {

			if (rm.hasRole("stop") || rm.hasRole("stop_entry_only") || rm.hasRole("stop_exit_only")
					|| rm.hasRole("platform") || rm.hasRole("platform_entry_only")
					|| rm.hasRole("platform_exit_only")) {

				if (rm.getNode().hasTag("public_transport", "stop_position")
						|| rm.getNode().hasTag("highway", "bus_stop")
						|| rm.getNode().hasTag("public_transport", "platform")
						|| rm.getNode().hasTag("public_transport", "platform_entry_only")
						|| rm.getNode().hasTag("public_transport", "platform_exit_only")
						|| rm.getNode().hasTag("highway", "platform")
						|| rm.getNode().hasTag("highway", "platform_entry_only")
						|| rm.getNode().hasTag("highway", "platform_exit_only")
						|| rm.getNode().hasTag("railway", "platform")
						|| rm.getNode().hasTag("railway", "platform_entry_only")
						|| rm.getNode().hasTag("railway", "platform_exit_only")) {
					return true;
				}
			}
		}

		if (rm.getType().equals(OsmPrimitiveType.WAY)) {
			if (rm.getWay().hasTag("public_transport", "platform")
					|| rm.getWay().hasTag("public_transport", "platform_entry_only")
					|| rm.getWay().hasTag("public_transport", "platform_exit_only")
					|| rm.getWay().hasTag("highway", "platform") || rm.getWay().hasTag("highway", "platform_entry_only")
					|| rm.getWay().hasTag("highway", "platform_exist_only") || rm.getWay().hasTag("railway", "platform")
					|| rm.getWay().hasTag("railway", "platform_entry_only")
					|| rm.getWay().hasTag("railway", "platform_exit_only")) {
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
	 * Checks if the type of the way is suitable for buses to go on it. The
	 * direction of the way (i.e. one-way roads) is irrelevant for this test.
	 * 
	 * TODO: this test is duplicated in WayChecker, remove it here when the old
	 * implementation is not needed anymore.
	 * 
	 * @deprecated
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

	public static boolean hasIncompleteMembers(Relation r) {
		if (r == null) {
			return true;
		}
		for (RelationMember rm : r.getMembers()) {
			if ((rm.isNode() && rm.getNode().isIncomplete()) || (rm.isWay() && rm.getWay().isIncomplete())
					|| (rm.isRelation() && rm.getRelation().isIncomplete())) {
				return true;
			}
		}

		return false;
	}

}
