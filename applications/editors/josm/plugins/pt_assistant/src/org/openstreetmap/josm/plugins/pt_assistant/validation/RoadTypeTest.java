package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class RoadTypeTest extends Test {

	public static final int ERROR_CODE_ROAD_TYPE = 3721;

	public RoadTypeTest() {
		super(tr("Road Type Test"),
				tr("Checks if the course of the route relation is compatible with the type of the road it passes on."));
	}

	@Override
	public void visit(Relation r) {

		if (RouteUtils.isTwoDirectionRoute(r)) {

			List<RelationMember> members = r.getMembers();

			for (RelationMember rm : members) {
				if (RouteUtils.isPTWay(rm)) {

					Way way = rm.getWay();
					// at this point, the relation has already been checked to
					// be a route of public_transport:version 2
					boolean isCorrectRoadType = true;
					if (r.hasTag("route", "bus") || r.hasTag("route", "share_taxi")) {
						if (!RouteUtils.isWaySuitableForBuses(way)) {
							isCorrectRoadType = false;
						}
					} else if (r.hasTag("route", "trolleybus")) {
						if (!(RouteUtils.isWaySuitableForBuses(way) && way.hasTag("trolley_wire", "yes"))) {
							isCorrectRoadType = false;
						}
					} else if (r.hasTag("route", "tram")) {
						if (!r.hasTag("railway", "tram")) {
							isCorrectRoadType = false;
						}
					} else if (r.hasTag("route", "subway")) {
						if (!r.hasTag("railway", "subway")) {
							isCorrectRoadType = false;
						}
					} else if (r.hasTag("route", "light_rail")) {
						if (!r.hasTag("raiilway", "subway")) {
							isCorrectRoadType = false;
						}
					} else if (r.hasTag("route", "light_rail")) {
						if (!r.hasTag("railway", "light_rail")) {
							isCorrectRoadType = false;
						}
					} else if (r.hasTag("route", "train")) {
						if (!r.hasTag("railway", "train")) {
							isCorrectRoadType = false;
						}
					}

					if (!isCorrectRoadType) {
						List<OsmPrimitive> primitiveList = new ArrayList<>(2);
						primitiveList.add(0, r);
						primitiveList.add(1, way);
						
						errors.add(new TestError(this, Severity.WARNING,
								tr("PT: Route type does not match the type of the road it passes on"),
								ERROR_CODE_ROAD_TYPE, primitiveList));
					}

				}
			}
		}
	}

	@Override
	public Command fixError(TestError testError) {

		List<Command> commands = new ArrayList<>(50);

		if (testError.getTester().getClass().equals(GapTest.class) && testError.isFixable()) {
			List<OsmPrimitive> primitiveList = (List<OsmPrimitive>) testError.getPrimitives();
			Relation originalRelation = (Relation) primitiveList.get(0);
			Way wayToRemove = (Way) primitiveList.get(1);
			
			Relation modifiedRelation = new Relation(originalRelation);
			List<RelationMember> modifiedRelationMembers = new ArrayList<>(originalRelation.getMembersCount()-1);
			
			// copy stop-related members first, public transport ways last:
			for (RelationMember rm: originalRelation.getMembers()) {
				if (RouteUtils.isPTStop(rm)) {
					modifiedRelationMembers.add(rm);
				} 
			}
			
			for (RelationMember rm: originalRelation.getMembers()) {
				if (RouteUtils.isPTWay(rm)) {
					Way wayToCheck = rm.getWay();
					if (wayToCheck != wayToRemove) {
						modifiedRelationMembers.add(rm);
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

		return new SequenceCommand(tr("Remove way from route if it does not match the route type"), commands);	}

	/**
	 * Checks if the test error is fixable
	 */
	@Override
	public boolean isFixable(TestError testError) {
		if (testError.getCode() == ERROR_CODE_ROAD_TYPE) {
			return true;
		}
		return false; 
	}

}
