package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Performs tests of a route at the level of single ways: DirectionTest and
 * RoadTypeTest
 * 
 * @author darya
 *
 */
public class WayChecker extends Checker {

	public WayChecker(Relation relation, Test test) {

		super(relation, test);

	}

	protected void performRoadTypeTest() {

		if (!relation.hasTag("route", "bus") && !relation.hasTag("route", "trolleybus")
				&& !relation.hasTag("route", "share_taxi")) {
			return;
		}

		for (RelationMember rm : relation.getMembers()) {
			if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {

				Way way = rm.getWay();
				// at this point, the relation has already been checked to
				// be a route of public_transport:version 2

				boolean isCorrectRoadType = true;
				boolean isUnderConstruction = false;
				if (relation.hasTag("route", "bus") || relation.hasTag("route", "share_taxi")) {
					if (!isWaySuitableForBuses(way)) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("highway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (relation.hasTag("route", "trolleybus")) {
					if (!(isWaySuitableForBuses(way) && way.hasTag("trolley_wire", "yes"))) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("highway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (relation.hasTag("route", "tram")) {
					if (!way.hasTag("railway", "tram")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (relation.hasTag("route", "subway")) {
					if (!relation.hasTag("railway", "subway")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (relation.hasTag("route", "light_rail")) {
					if (!relation.hasTag("raiilway", "subway")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (relation.hasTag("route", "light_rail")) {
					if (!relation.hasTag("railway", "light_rail")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				} else if (relation.hasTag("route", "train")) {
					if (!relation.hasTag("railway", "train")) {
						isCorrectRoadType = false;
					}
					if (way.hasTag("railway", "construction") && way.hasKey("construction")) {
						isUnderConstruction = true;
					}
				}

				if (!isCorrectRoadType && !isUnderConstruction) {

					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(relation);
					List<Way> highlighted = new ArrayList<>(1);
					highlighted.add(way);
					TestError e = new TestError(this.test, Severity.WARNING,
							tr("PT: Route type does not match the type of the road it passes on"),
							PTAssistantValidatorTest.ERROR_CODE_ROAD_TYPE, primitives, highlighted);
					errors.add(e);

				}

				if (isUnderConstruction) {
					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(relation);
					List<Way> highlighted = new ArrayList<>(1);
					highlighted.add(way);
					TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Road is under construction"),
							PTAssistantValidatorTest.ERROR_CODE_CONSTRUCTION, primitives, highlighted);
					errors.add(e);
				}
			}
		}

	}

	protected void performDirectionTest() {

		List<Way> waysToCheck = new ArrayList<>();

		for (RelationMember rm : relation.getMembers()) {
			if (RouteUtils.isPTWay(rm)) {
				if (rm.isWay()) {
					waysToCheck.add(rm.getWay());
				} else {
					Relation nestedRelation = rm.getRelation();
					for (RelationMember nestedRelationMember : nestedRelation.getMembers()) {
						waysToCheck.add(nestedRelationMember.getWay());
					}
				}
			}
		}

		if (waysToCheck.size() <= 1) {
			return;
		}

		List<Way> problematicWays = new ArrayList<>();

		for (int i = 0; i < waysToCheck.size(); i++) {

			Way curr = waysToCheck.get(i);

			if (i == 0) {
				// first way:
				Way next = waysToCheck.get(i + 1);
				if (!touchCorrectly(null, curr, next)) {
					problematicWays.add(curr);
				}

			} else if (i == waysToCheck.size() - 1) {
				// last way:
				Way prev = waysToCheck.get(i - 1);
				if (!touchCorrectly(prev, curr, null)) {
					problematicWays.add(curr);
				}

			} else {
				// all other ways:
				Way prev = waysToCheck.get(i - 1);
				Way next = waysToCheck.get(i + 1);
				if (!touchCorrectly(prev, curr, next)) {
					problematicWays.add(curr);
				}
			}
		}

		for (Way problematicWay : problematicWays) {

			List<Relation> primitives = new ArrayList<>(1);
			primitives.add(relation);
			List<Way> highlighted = new ArrayList<>(1);
			highlighted.add(problematicWay);
			TestError e = new TestError(this.test, Severity.WARNING,
					tr("PT: Route passes a oneway road in the wrong direction"),
					PTAssistantValidatorTest.ERROR_CODE_DIRECTION, primitives, highlighted);
			this.errors.add(e);
		}

	}

	/**
	 * Checks if the type of the way is suitable for buses to go on it. The
	 * direction of the way (i.e. one-way roads) is irrelevant for this test.
	 * 
	 * @param way
	 *            to be checked
	 * @return true if the way is suitable for buses, false otherwise.
	 */
	private boolean isWaySuitableForBuses(Way way) {
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

	protected static Command fixErrorByRemovingWay(TestError testError) {

		if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_ROAD_TYPE
				&& testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_DIRECTION) {
			return null;
		}

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

		return changeCommand;
	}

	protected static Command fixErrorByZooming(TestError testError) {

		if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_DIRECTION) {
			return null;
		}

		ArrayList<Command> commands = new ArrayList<>();

		Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
		Relation originalRelation = (Relation) primitives.iterator().next();
		ArrayList<OsmPrimitive> primitivesToSelect = new ArrayList<>(1);
		primitivesToSelect.add(originalRelation);
		Collection<?> highlighted = testError.getHighlighted();
		Way wayToHighlight = (Way) highlighted.iterator().next();
		ArrayList<OsmPrimitive> primitivesToZoom = new ArrayList<>(1);
		primitivesToZoom.add(wayToHighlight);

		SelectCommand command1 = new SelectCommand(primitivesToSelect);
		commands.add(command1);
		SelectCommand command2 = new SelectCommand(primitivesToZoom);
		commands.add(command2);

		List<OsmDataLayer> listOfLayers = Main.getLayerManager().getLayersOfType(OsmDataLayer.class);
		for (OsmDataLayer osmDataLayer : listOfLayers) {
			if (osmDataLayer.data == originalRelation.getDataSet()) {

				final OsmDataLayer layerParameter = osmDataLayer;
				final Relation relationParameter = originalRelation;
				final Collection<OsmPrimitive> zoomParameter = primitivesToZoom;

				if (SwingUtilities.isEventDispatchThread()) {

					showRelationEditorAndZoom(layerParameter, relationParameter, zoomParameter);

				} else {

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {

							showRelationEditorAndZoom(layerParameter, relationParameter, zoomParameter);

						}
					});

				}

				return new SequenceCommand(null, commands);
			}
		}

		return null;

	}

	private static void showRelationEditorAndZoom(OsmDataLayer layer, Relation r, Collection<OsmPrimitive> primitives) {

		// zoom to problem:
		AutoScaleAction.zoomTo(primitives);

		// create editor:
		GenericRelationEditor editor = (GenericRelationEditor) RelationEditor.getEditor(layer, r,
				r.getMembersFor(primitives));

		// put stop-related members to the front and edit roles if necessary:
		List<RelationMember> sortedRelationMembers = listStopMembers(r);
		sortedRelationMembers.addAll(listNotStopMembers(r));
		r.setMembers(sortedRelationMembers);
		editor.reloadDataFromRelation();

		// open editor:
		editor.setVisible(true);

	}

	/**
	 * Checks if the current way touches its neighbouring was correctly
	 * 
	 * @param prev
	 *            can be null
	 * @param curr
	 *            cannot be null
	 * @param next
	 *            can be null
	 * @return
	 */
	private boolean touchCorrectly(Way prev, Way curr, Way next) {

		if (RouteUtils.isOnewayForPublicTransport(curr) == 0) {
			return true;
		}

		if (prev != null) {

			if (RouteUtils.waysTouch(curr, prev)) {
				Node nodeInQuestion;
				if (RouteUtils.isOnewayForPublicTransport(curr) == 1) {
					nodeInQuestion = curr.firstNode();
				} else {
					nodeInQuestion = curr.lastNode();
				}

				if (nodeInQuestion != prev.firstNode() && nodeInQuestion != prev.lastNode()) {
					return false;
				}
			}
		}

		if (next != null) {

			if (RouteUtils.waysTouch(curr, next)) {
				Node nodeInQuestion;
				if (RouteUtils.isOnewayForPublicTransport(curr) == 1) {
					nodeInQuestion = curr.lastNode();
				} else {
					nodeInQuestion = curr.firstNode();
				}

				if (nodeInQuestion != next.firstNode() && nodeInQuestion != next.lastNode()) {
					return false;
				}
			}
		}

		return true;

	}

}
