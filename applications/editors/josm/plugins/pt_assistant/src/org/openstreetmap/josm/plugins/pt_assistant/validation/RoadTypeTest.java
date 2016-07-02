package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class RoadTypeTest extends Test {

	public static final int ERROR_CODE_ROAD_TYPE = 3721;
	public static final int ERROR_CODE_CONSTRUCTION = 3722;

	public RoadTypeTest() {
		super(tr("Road Type Test"),
				tr("Checks if the course of the route relation is compatible with the type of the road it passes on."));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void visit(Relation r) {

		if (!RouteUtils.isTwoDirectionRoute(r)) {
			return;
		}
		
		if (RouteUtils.hasIncompleteMembers(r)) {
			return;
		}
		
		List<RelationMember> members = r.getMembers();

		for (RelationMember rm : members) {
			if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {

				Way way = rm.getWay();
				// at this point, the relation has already been checked to
				// be a route of public_transport:version 2
				boolean isCorrectRoadType = true;
				boolean isUnderConstruction = false;

				if (r.hasTag("route", "bus") || r.hasTag("route", "share_taxi")) {
					if (!RouteUtils.isWaySuitableForBuses(way)) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("highway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (r.hasTag("route", "trolleybus")) {
					if (!(RouteUtils.isWaySuitableForBuses(way) && way.hasTag("trolley_wire", "yes"))) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("highway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (r.hasTag("route", "tram")) {
					if (!way.hasTag("railway", "tram")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (r.hasTag("route", "subway")) {
					if (!r.hasTag("railway", "subway")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (r.hasTag("route", "light_rail")) {
					if (!r.hasTag("raiilway", "subway")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (r.hasTag("route", "light_rail")) {
					if (!r.hasTag("railway", "light_rail")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (r.hasTag("route", "train")) {
					if (!r.hasTag("railway", "train")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				}

				if (!isCorrectRoadType && !isUnderConstruction) {

					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(r);
					List<Way> highlighted = new ArrayList<>(1);
					highlighted.add(way);
					errors.add(new TestError(this, Severity.WARNING,
							tr("PT: Route type does not match the type of the road it passes on"), ERROR_CODE_ROAD_TYPE,
							primitives, highlighted));
				}
				
				if (isUnderConstruction) {
					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(r);
					List<Way> highlighted = new ArrayList<>(1);
					highlighted.add(way);
					TestError e = new TestError(this, Severity.WARNING,
							tr("PT: Road is under construction"),
							PTAssitantValidatorTest.ERROR_CODE_CONSTRUCTION, primitives, highlighted);
					errors.add(e);
				}

			}
		}

	}

	@Override
	public Command fixError(TestError testError) {

		List<Command> commands = new ArrayList<>(50);

		if (testError.getTester().getClass().equals(RoadTypeTest.class) && testError.isFixable()) {
			Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
			Relation originalRelation = (Relation) primitives.iterator().next();
			Collection<?> highlighted = testError.getHighlighted();
			Way wayToRemove = (Way) highlighted.iterator().next();

			Relation modifiedRelation = new Relation(originalRelation);
			List<RelationMember> modifiedRelationMembers = new ArrayList<>(originalRelation.getMembersCount() - 1);

			// copy PT stops first, PT ways last:
			for (RelationMember rm : originalRelation.getMembers()) {
				if (RouteUtils.isPTStop(rm)) {

					if (rm.getRole().equals("stop_position")) {
						if (rm.getType().equals(OsmPrimitiveType.NODE)) {
							RelationMember newMember = new RelationMember("stop", rm.getNode());
							modifiedRelationMembers.add(newMember);
						} else { // if it is a way:
							RelationMember newMember = new RelationMember("stop", rm.getWay());
							modifiedRelationMembers.add(newMember);
						}
					} else {
						// if the relation member does not have the role
						// "stop_position":
						modifiedRelationMembers.add(rm);
					}

				}
			}

			// now copy PT ways:
			for (RelationMember rm : originalRelation.getMembers()) {
				if (RouteUtils.isPTWay(rm)) {
					Way wayToCheck = rm.getWay();
					if (wayToCheck != wayToRemove) {
						if (rm.getRole().equals("forward") || rm.getRole().equals("backward")) {
							RelationMember modifiedMember = new RelationMember("", wayToCheck);
							modifiedRelationMembers.add(modifiedMember);
						} else {
							modifiedRelationMembers.add(rm);
						}
					}
				}
			}

			modifiedRelation.setMembers(modifiedRelationMembers);

			ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);
			commands.add(changeCommand);

		}

		if (commands.isEmpty()) {
			return null;
		}

		if (commands.size() == 1) {
			return commands.get(0);
		}

		return new SequenceCommand(tr("Remove way from route if it does not match the route type"), commands);

	}

	/**
	 * Checks if the test error is fixable
	 */
	@Override
	public boolean isFixable(TestError testError) {
		if (testError.getCode() == ERROR_CODE_ROAD_TYPE || testError.getCode() == ERROR_CODE_CONSTRUCTION) {
			return true;
		}
		return false;
	}

}
