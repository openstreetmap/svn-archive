package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
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
	private static List<PTRouteSegment> correctSegments = new ArrayList<PTRouteSegment>();

	/* Manager of the PTStops and PTWays of the current route */
	private PTRouteDataManager manager;

	/* Assigns PTStops to nearest PTWays and stores that correspondence */
	private StopToWayAssigner assigner;

	/*
	 * Stores reference that shows in which direction the segment checker is
	 * moving
	 */
	private Node firstNodeOfRouteSegmentInDirectionOfTravel;

	public SegmentChecker(Relation relation, Test test) {

		super(relation, test);

		this.manager = new PTRouteDataManager(relation);

		for (RelationMember rm : manager.getFailedMembers()) {
			List<Relation> primitives = new ArrayList<>(1);
			primitives.add(relation);
			List<OsmPrimitive> highlighted = new ArrayList<>(1);
			highlighted.add(rm.getMember());
			TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Relation member roles do not match tags"),
					PTAssistantValidatorTest.ERROR_CODE_RELAITON_MEMBER_ROLES, primitives, highlighted);
			this.errors.add(e);
		}

		this.assigner = new StopToWayAssigner(manager.getPTWays());

	}

	/**
	 * Returns the number of route segments that have been already successfully
	 * verified
	 * 
	 * @return
	 */
	public static int getCorrectSegmentCount() {
		return correctSegments.size();
	}

	/**
	 * Adds the given correct segment to the list of correct segments without
	 * checking its correctness
	 * 
	 * @param segment
	 *            to add to the list of correct segments
	 */
	public static void addCorrectSegment(PTRouteSegment segment) {
		correctSegments.add(segment);
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
						PTAssistantValidatorTest.ERROR_CODE_END_STOP, primitives, highlighted);
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
						PTAssistantValidatorTest.ERROR_CODE_SPLIT_WAY, primitives, highlighted);
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
						PTAssistantValidatorTest.ERROR_CODE_SPLIT_WAY, primitives, highlighted);
				this.errors.add(e);
			}
		}

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

	public void performStopByStopTest() {

		if (manager.getPTStopCount() < 2) {
			return;
		}

		// Check each route segment:
		for (int i = 1; i < manager.getPTStopCount(); i++) {

			PTStop startStop = manager.getPTStops().get(i - 1);
			PTStop endStop = manager.getPTStops().get(i);

			Way startWay = assigner.get(startStop);
			Way endWay = assigner.get(endStop);
			if (startWay == null) {
				this.firstNodeOfRouteSegmentInDirectionOfTravel = null;
				continue;
			}
			if (endWay == null) {
				this.firstNodeOfRouteSegmentInDirectionOfTravel = null;
				continue;
			}
			// FIXME: throw error if cannot find the corresponding way (which
			// means that the stop is too far away from way)
			List<PTWay> segmentWays = manager.getPTWaysBetween(startWay, endWay);

			if (this.firstNodeOfRouteSegmentInDirectionOfTravel == null) {
				// if we are at the beginning of the route or after a gap /
				// error:

				this.firstNodeOfRouteSegmentInDirectionOfTravel = findFirstNodeOfRouteSegmentInDirectionOfTravel(
						segmentWays.get(0));
				if (this.firstNodeOfRouteSegmentInDirectionOfTravel == null) {
					// TODO: throw error
					continue;
				}
			}

			boolean sortingCorrect = existingWaySortingIsCorrect(segmentWays.get(0),
					this.firstNodeOfRouteSegmentInDirectionOfTravel, segmentWays.get(segmentWays.size() - 1));
			if (sortingCorrect) {
				PTRouteSegment routeSegment = new PTRouteSegment(startStop, endStop, segmentWays);
				correctSegments.add(routeSegment);
			}
		}

	}

	private Node findFirstNodeOfRouteSegmentInDirectionOfTravel(PTWay startWay) {

		// 1) at first check if one of the first or last node of the first ptway
		// is a deadend node:
		Node[] startWayEndnodes = startWay.getEndNodes();
		if (isDeadendNode(startWayEndnodes[0])) {
			return startWayEndnodes[0];
		}
		if (isDeadendNode(startWayEndnodes[1])) {
			return startWayEndnodes[1];
		}

		// 2) failing that, check which node this startWay shares with the
		// following way:
		PTWay nextWay = manager.getNextPTWay(startWay);
		if (nextWay == null) {
			return null;
		}
		Node[] nextWayEndnodes = nextWay.getEndNodes();
		if (startWayEndnodes[0] == nextWayEndnodes[0] || startWayEndnodes[0] == nextWayEndnodes[1]) {
			return startWayEndnodes[1];
		}
		if (startWayEndnodes[1] == nextWayEndnodes[0] || startWayEndnodes[1] == nextWayEndnodes[1]) {
			return startWayEndnodes[0];
		}

		return null;

	}

	private boolean isDeadendNode(Node node) {
		int count = 0;
		for (PTWay ptway : manager.getPTWays()) {
			List<Way> ways = ptway.getWays();
			for (Way way : ways) {
				if (way.firstNode() == node || way.lastNode() == node) {
					count++;
				}
			}
		}
		return count == 1;
	}

	/**
	 * Finds the deadend node closest to the given node represented by its
	 * coordinates
	 * 
	 * @param coord
	 *            coordinates of the givenn node
	 * @param deadendNodes
	 * @return the closest deadend node
	 */
	@SuppressWarnings("unused")
	private Node findClosestDeadendNode(LatLon coord, List<Node> deadendNodes) {

		Node closestDeadendNode = null;
		double minSqDistance = Double.MAX_VALUE;
		for (Node deadendNode : deadendNodes) {
			double distanceSq = coord.distanceSq(deadendNode.getCoor());
			if (distanceSq < minSqDistance) {
				minSqDistance = distanceSq;
				closestDeadendNode = deadendNode;
			}
		}
		return closestDeadendNode;

	}

	private boolean existingWaySortingIsCorrect(PTWay start, Node startWayPreviousNodeInDirectionOfTravel, PTWay end) {

		if (start == end) {
			// if both PTStops are on the same PTWay
			return true;
		}

		PTWay current = start;

		while (!current.equals(end)) {
			// "equals" is used here instead of "==" because when the same way
			// is passed multiple times by the bus, the algorithm should stop no
			// matter which of the geometrically equal PTWays it finds

			// find the next node in direction of travel (which is part of the
			// PTWay start):
			firstNodeOfRouteSegmentInDirectionOfTravel = getOppositeEndNode(current,
					firstNodeOfRouteSegmentInDirectionOfTravel);

			List<PTWay> nextWaysInDirectionOfTravel = this.findNextPTWaysInDirectionOfTravel(current,
					firstNodeOfRouteSegmentInDirectionOfTravel);
	
			PTWay nextPTWayAccortingToExistingSorting = manager.getNextPTWay(current);
			if (!nextWaysInDirectionOfTravel.contains(nextPTWayAccortingToExistingSorting)) {
				List<Relation> primitives = new ArrayList<>(1);
				primitives.add(relation);
				List<OsmPrimitive> highlighted = new ArrayList<>();

				highlighted.addAll(current.getWays());
				highlighted.add(firstNodeOfRouteSegmentInDirectionOfTravel);

				TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Problem in the route segment"),
						PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP, primitives, highlighted);
				this.errors.add(e);
				this.firstNodeOfRouteSegmentInDirectionOfTravel = null;
				return false;
			}

			current = nextPTWayAccortingToExistingSorting;

		}

		return true;
	}

	private Node getOppositeEndNode(Way way, Node node) {

		if (node == way.firstNode()) {
			return way.lastNode();
		}

		if (node == way.lastNode()) {
			return way.firstNode();
		}

		return null;
	}

	private Node getOppositeEndNode(PTWay ptway, Node node) {
		if (ptway.isWay()) {
			return getOppositeEndNode(ptway.getWays().get(0), node);
		}

		Way firstWay = ptway.getWays().get(0);
		Way lastWay = ptway.getWays().get(ptway.getWays().size() - 1);
		Node oppositeNode = node;
		if (firstWay.firstNode() == node || firstWay.lastNode() == node) {
			for (int i = 0; i < ptway.getWays().size(); i++) {
				oppositeNode = getOppositeEndNode(ptway.getWays().get(i), oppositeNode);
			}
			return oppositeNode;
		} else if (lastWay.firstNode() == node || lastWay.lastNode() == node) {
			for (int i = ptway.getWays().size() - 1; i >= 0; i--) {
				oppositeNode = getOppositeEndNode(ptway.getWays().get(i), oppositeNode);
			}
			return oppositeNode;
		}

		return null;

	}

	/**
	 * 
	 * @param way
	 * @param nodeInDirectionOfTravel
	 * @return
	 */
	private List<PTWay> findNextPTWaysInDirectionOfTravel(PTWay currentWay, Node nextNodeInDirectionOfTravel) {

		List<PTWay> nextPtways = new ArrayList<>();

		List<PTWay> ptways = manager.getPTWays();

		for (PTWay ptway : ptways) {

			if (ptway != currentWay) {
				Node[] endNodes = ptway.getEndNodes();
				if (endNodes[0] == nextNodeInDirectionOfTravel || endNodes[1] == nextNodeInDirectionOfTravel) {
					nextPtways.add(ptway);
				}
			}
		}

		return nextPtways;

	}

	protected static Command fixError(TestError testError) {

		// FIXME

		return null;
	}

}