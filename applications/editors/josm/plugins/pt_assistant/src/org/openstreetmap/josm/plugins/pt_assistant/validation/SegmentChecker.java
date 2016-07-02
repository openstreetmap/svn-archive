package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;

/**
 * Performs tests of a route at the level of route segments (the stop-by-stop
 * approach).
 * 
 * @author darya
 *
 */
public class SegmentChecker extends Checker {

	/* PTRouteSegments that have been validated and are correct */
	@SuppressWarnings("unused")
	private static List<PTRouteSegment> correctSegments = new ArrayList<PTRouteSegment>();

	/* Manager of the PTStops and PTWays of the current route */
	private PTRouteDataManager manager;

	/* Assigns PTStops to nearest PTWays and stores that correspondence */
	@SuppressWarnings("unused")
	private StopToWayAssigner assigner;

	public SegmentChecker(Relation relation, Test test) {

		super(relation, test);

		this.manager = new PTRouteDataManager(relation);
		
		for (RelationMember rm: manager.getFailedMembers()) {
			List<Relation> primitives = new ArrayList<>(1);
			primitives.add(relation);
			List<OsmPrimitive> highlighted = new ArrayList<>(1);
			highlighted.add(rm.getMember());
			TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Relation member roles do not match tags"),
					PTAssitantValidatorTest.ERROR_CODE_RELAITON_MEMBER_ROLES, primitives, highlighted);
			this.errors.add(e);
		}


		
		this.assigner = new StopToWayAssigner(manager.getPTWays());

	}

	public void performFirstStopTest() {

		performEndStopTest(manager.getFirstStop());

	}

	public void performLastStopTest() {

		performEndStopTest(manager.getLastStop());

	}

	private void performEndStopTest(PTStop endStop) {

		if (endStop == null) {
			return;
		}

		/*
		 * This test checks: (1) that a stop position exists; (2) that it is the
		 * first or last node of its parent ways which belong to this route.
		 */

		if (endStop.getStopPosition() == null) {

			List<Node> potentialStopPositionList = endStop.findPotentialStopPositions();
			List<Node> stopPositionsOfThisRoute = new ArrayList<>();
			boolean containsAtLeastOneStopPositionAsFirstOrLastNode = false;

			for (Node potentialStopPosition : potentialStopPositionList) {

				int belongsToWay = belongsToAWayOfThisRoute(potentialStopPosition);

				if (belongsToWay == 0) {
					stopPositionsOfThisRoute.add(potentialStopPosition);
					containsAtLeastOneStopPositionAsFirstOrLastNode = true;
				}

				if (belongsToWay == 1) {
					stopPositionsOfThisRoute.add(potentialStopPosition);
				}
			}

			if (stopPositionsOfThisRoute.isEmpty()) {
				List<Relation> primitives = new ArrayList<>(1);
				primitives.add(relation);
				List<OsmPrimitive> highlighted = new ArrayList<>(1);
				highlighted.add(endStop.getPlatform());
				TestError e = new TestError(this.test, Severity.WARNING,
						tr("PT: Route should start and end with a stop_position"),
						PTAssitantValidatorTest.ERROR_CODE_END_STOP, primitives, highlighted);
				this.errors.add(e);
				return;
			}

			if (stopPositionsOfThisRoute.size() == 1) {
				endStop.setStopPosition(stopPositionsOfThisRoute.get(0));
			}

			// At this point, there is at least one stop_position for this
			// endStop:
			if (!containsAtLeastOneStopPositionAsFirstOrLastNode) {
				List<Relation> primitives = new ArrayList<>(1);
				primitives.add(relation);
				List<OsmPrimitive> highlighted = new ArrayList<>();
				highlighted.addAll(stopPositionsOfThisRoute);

				TestError e = new TestError(this.test, Severity.WARNING, tr("PT: First or last way needs to be split"),
						PTAssitantValidatorTest.ERROR_CODE_SPLIT_WAY, primitives, highlighted);
				this.errors.add(e);
			}

		} else {

			// if the stop_position is known:
			int belongsToWay = this.belongsToAWayOfThisRoute(endStop.getStopPosition());

			if (belongsToWay == 1) {

				List<Relation> primitives = new ArrayList<>(1);
				primitives.add(relation);
				List<OsmPrimitive> highlighted = new ArrayList<>();
				highlighted.add(endStop.getStopPosition());
				TestError e = new TestError(this.test, Severity.WARNING, tr("PT: First or last way needs to be split"),
						PTAssitantValidatorTest.ERROR_CODE_SPLIT_WAY, primitives, highlighted);
				this.errors.add(e);
			}
		}

	}

	@SuppressWarnings("unused")
	private void performSegmentTest() {
		// TODO
	}

	/**
	 * Checks if the given node belongs to the ways of this route.
	 * 
	 * @param node
	 *            Node to be checked
	 * @return 1 if belongs only as an inner node, 0 if belongs as a first or
	 *         last node for at least one way, -1 if does not belong to any way.
	 */
	private int belongsToAWayOfThisRoute(Node node) {

		boolean contains = false;

		List<PTWay> ptways = manager.getPTWays();
		for (PTWay ptway : ptways) {
			List<Way> ways = ptway.getWays();
			for (Way way : ways) {
				if (way.containsNode(node)) {

					if (way.firstNode().equals(node) || way.lastNode().equals(node)) {
						return 0;
					}

					contains = true;
				}
			}
		}

		if (contains) {
			return 1;
		}

		return -1;
	}

}