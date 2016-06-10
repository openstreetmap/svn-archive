package org.openstreetmap.josm.plugins.pt_assistant.utils;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.DownloadPrimitiveAction;
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

	// indicates if the user needs to be asked before fetching incomplete
	// members of a relation.

	private enum ASK_TO_FETCH {
		DO_ASK, DONT_ASK_AND_FETCH, DONT_ASK_AND_DONT_FETCH
	};

	private static ASK_TO_FETCH askToFetch = ASK_TO_FETCH.DO_ASK;
	
	// checks that the same relation is only fetched once
	private static Relation lastRelationToFetch = null;

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
			return true;
		}

		if (rm.getType().equals(OsmPrimitiveType.RELATION)) {
			if (rm.getRole().equals("stop_area")) {
				return true;
			} else {
				return false;
			}
		}

		Way w = rm.getWay();

		if (w.hasTag("public_transport", "platform") || w.hasTag("highway", "platform")
				|| w.hasTag("railway", "platform") || w.hasTag("public_transport", "platform_entry_only")
				|| w.hasTag("highway", "platform_entry_only") || w.hasTag("railway", "platform_entry_only")
				|| w.hasTag("public_transport", "platform_exit_only") || w.hasTag("highway", "platform_exit_only")
				|| w.hasTag("railway", "platform_exit_only")) {
			return true;
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

		return !isPTStop(rm);
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

	/**
	 * Checks if all members of a relation are complete. If not, the user is
	 * asked to confirm the permission to fetch them, and they are fetched. The
	 * completeness of the relation itself is not checked.
	 * 
	 * @param r
	 *            relation
	 * @return true if all relation members are complete (or fetched), false
	 *         otherwise (including if the user denies permission to download
	 *         data)
	 * TODO: what should be done in case the connection to the server is broken
	 */
	public static boolean ensureMemberCompleteness(Relation r) {
		
		if (r == null) {
			return false;
		}

		boolean isComplete = true;

		// check if there is at least one incomplete relation member:
		for (RelationMember rm : r.getMembers()) {
			if ((rm.isNode() && rm.getNode().isIncomplete()) || (rm.isWay() && rm.getWay().isIncomplete())
					|| (rm.isRelation() && rm.getRelation().isIncomplete())) {
				isComplete = false;
				break;
			}
		}

		if (!isComplete && !r.equals(lastRelationToFetch)) {

			int userInput = Integer.MIN_VALUE;
			

			if (askToFetch == ASK_TO_FETCH.DO_ASK) {
				String message = tr("The relation (id=" + r.getId()
						+ ") has incomplete members.\nThey need to be downloaded to proceed with validation of this relation.\nDo you want to download incomplete members?");
				JCheckBox checkbox = new JCheckBox(tr("Remember my choice and don't ask me again in this session"));
				Object[] params = { message, checkbox };
				String[] options = { tr("Yes"), tr("No") };
				// ask the user:
				userInput = JOptionPane.showOptionDialog(null, params, tr("Fetch Request"), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, 0);
				

				// if the user does not want to be asked:
				if (checkbox.isSelected()) {
					if (userInput == 0) {
						askToFetch = ASK_TO_FETCH.DONT_ASK_AND_FETCH;
					} else {
						askToFetch = ASK_TO_FETCH.DONT_ASK_AND_DONT_FETCH;
					}
				}
			}

			// if the user does want to fetch:
			if (userInput == 0 || askToFetch == ASK_TO_FETCH.DONT_ASK_AND_FETCH) {
				List<PrimitiveId> list = new ArrayList<>(1);
				list.add(r);
				DownloadPrimitiveAction.processItems(false, list, false, true);
				isComplete = true;
				lastRelationToFetch = r;

			}

		}

		return isComplete;
	}
	
	
	public static boolean hasIncompleteMembers(Relation r) {
		if (r == null) {
			return true;
		}
		for (RelationMember rm: r.getMembers()) {
			if ((rm.isNode() && rm.getNode().isIncomplete()) || (rm.isWay() && rm.getWay().isIncomplete())
					|| (rm.isRelation() && rm.getRelation().isIncomplete())) {
				return true;
			}
		}
		
		return false;
	}
	
//	/**
//	 * TODO: this is temporal
//	 */
//	public static String getFetch() {
//		if (askToFetch == ASK_TO_FETCH.DO_ASK) {
//			return "do ask";
//		} 
//		if (askToFetch == ASK_TO_FETCH.DONT_ASK_AND_FETCH) {
//			return "don;t ask and fetch";
//		}
//		return "don't ask and don't fetch";
//	}

}
