package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.OsmUtils;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Performs the DirectionTest and RoadTypeTest at the level of single ways
 * 
 * @author darya
 *
 */
public class WayChecker {

	// test which created this WayChecker:
	private final Test test;

	// relation that is checked:
	private Relation relation;

	// stores all found errors (on way level):
	private ArrayList<TestError> errors = new ArrayList<>();

	// stores all ways that were found wrong and need to be removed:
	private ArrayList<Way> wrongWays = new ArrayList<>();

	public WayChecker(Relation r, Test test) {

		this.test = test;
		this.relation = r;
		
		this.performDirectionTest();
		this.performRoadTypeTest();
		
	}

	private void performRoadTypeTest() {
		
		if (!relation.hasTag("route", "bus") && !relation.hasTag("route", "trolleybus") && !relation.hasTag("route", "share_taxi")) {
			return;
		}

		for (RelationMember rm : relation.getMembers()) {
			if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {

				Way way = rm.getWay();
				// at this point, the relation has already been checked to
				// be a route of public_transport:version 2

				boolean isCorrectRoadType = true;
				if (relation.hasTag("route", "bus") || relation.hasTag("route", "share_taxi")) {
					if (!isWaySuitableForBuses(way)) {
						isCorrectRoadType = false;
					}
				} else if (relation.hasTag("route", "trolleybus")) {
					if (!(isWaySuitableForBuses(way) && way.hasTag("trolley_wire", "yes"))) {
						isCorrectRoadType = false;
					}
				} else if (relation.hasTag("route", "tram")) {
					if (!way.hasTag("railway", "tram")) {
						isCorrectRoadType = false;
					}
				} else if (relation.hasTag("route", "subway")) {
					if (!relation.hasTag("railway", "subway")) {
						isCorrectRoadType = false;
					}
				} else if (relation.hasTag("route", "light_rail")) {
					if (!relation.hasTag("raiilway", "subway")) {
						isCorrectRoadType = false;
					}
				} else if (relation.hasTag("route", "light_rail")) {
					if (!relation.hasTag("railway", "light_rail")) {
						isCorrectRoadType = false;
					}
				} else if (relation.hasTag("route", "train")) {
					if (!relation.hasTag("railway", "train")) {
						isCorrectRoadType = false;
					}
				}

				if (!isCorrectRoadType) {

					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(relation);
					List<Way> highlighted = new ArrayList<>(1);
					highlighted.add(way);
					TestError e = new TestError(this.test, Severity.WARNING,
							tr("PT: Route type does not match the type of the road it passes on"),
							PTAssitantValidatorTest.ERROR_CODE_ROAD_TYPE, primitives, highlighted);
					errors.add(e);

				}
			}
		}

	}

	private void performDirectionTest() {

		List<RelationMember> waysToCheck = new ArrayList<>();

		for (RelationMember rm : relation.getMembers()) {
			if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {
				waysToCheck.add(rm);
			}
		}

		if (waysToCheck.isEmpty()) {
			return;
		}

		WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
		final List<WayConnectionType> links = connectionTypeCalculator.updateLinks(waysToCheck);

		for (int i = 0; i < links.size(); i++) {
			if ((OsmUtils.isTrue(waysToCheck.get(i).getWay().get("oneway"))
					&& links.get(i).direction.equals(WayConnectionType.Direction.BACKWARD))
					|| (OsmUtils.isReversed(waysToCheck.get(i).getWay().get("oneway"))
							&& links.get(i).direction.equals(WayConnectionType.Direction.FORWARD))) {

				// At this point, the PTWay is going against the oneway
				// direction. Check if this road allows buses to disregard
				// the oneway restriction:

				if (!waysToCheck.get(i).getWay().hasTag("busway", "lane")
						&& !waysToCheck.get(i).getWay().hasTag("oneway:bus", "no")
						&& !waysToCheck.get(i).getWay().hasTag("busway", "opposite_lane")
						&& !waysToCheck.get(i).getWay().hasTag("oneway:psv", "no")
						&& !waysToCheck.get(i).getWay().hasTag("trolley_wire", "backward")) {
					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(relation);
					List<Way> highlighted = new ArrayList<>(1);
					highlighted.add(waysToCheck.get(i).getWay());
					TestError e = new TestError(this.test, Severity.WARNING,
							tr("PT: Route passes a oneway road in wrong direction"),
							PTAssitantValidatorTest.ERROR_CODE_DIRECTION, primitives, highlighted);
					this.errors.add(e);
					return;
				}

			}
		}

	}

	public List<TestError> getErrors() {

		return errors;
	}

	/**
	 * Checks if the type of the way is suitable for buses to go on it. The
	 * direction of the way (i.e. one-way roads) is irrelevant for this test.
	 * 
	 * @param way
	 *            to be checked
	 * @return true if the way is suitable for buses, false otherwise.
	 */
	public boolean isWaySuitableForBuses(Way way) {
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
