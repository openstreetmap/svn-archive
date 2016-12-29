// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.command.ChangeCommand;
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
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantLayer;
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
	private static List<PTRouteSegment> correctSegments = new ArrayList<>();

	/* PTRouteSegments that are wrong, stored in case the user calls the fix */
	protected static HashMap<TestError, PTRouteSegment> wrongSegments = new HashMap<>();
	protected static HashMap<Builder, PTRouteSegment> wrongSegmentBuilders = new HashMap<>();

	/* Manager of the PTStops and PTWays of the current route */
	private PTRouteDataManager manager;

	/* Assigns PTStops to nearest PTWays and stores that correspondence */
	private StopToWayAssigner assigner;

	public SegmentChecker(Relation relation, Test test) {

		super(relation, test);

		this.manager = new PTRouteDataManager(relation);

		for (RelationMember rm : manager.getFailedMembers()) {
			List<Relation> primitives = new ArrayList<>(1);
			primitives.add(relation);
			List<OsmPrimitive> highlighted = new ArrayList<>(1);
			highlighted.add(rm.getMember());
			Builder builder = TestError.builder(this.test, Severity.WARNING,
					PTAssistantValidatorTest.ERROR_CODE_RELAITON_MEMBER_ROLES);
			builder.message(tr("PT: Relation member roles do not match tags"));
			builder.primitives(primitives);
			builder.highlight(highlighted);
			TestError e = builder.build();
			this.errors.add(e);
		}

		this.assigner = new StopToWayAssigner(manager.getPTWays());

	}

	/**
	 * Returns the number of route segments that have been already successfully
	 * verified
	 *
	 * @return the number of route segments
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
	public static synchronized void addCorrectSegment(PTRouteSegment segment) {
		for (PTRouteSegment correctSegment : correctSegments) {
			if (correctSegment.equalsRouteSegment(segment)) {
				return;
			}
		}
		correctSegments.add(segment);
	}

	/**
	 * Used for unit tests
	 * 
	 * @param error
	 *            test error
	 * @return wrong route segment
	 */
	protected static PTRouteSegment getWrongSegment(TestError error) {
		return wrongSegments.get(error);
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
				Builder builder = TestError.builder(this.test, Severity.WARNING,
						PTAssistantValidatorTest.ERROR_CODE_END_STOP);
				builder.message(tr("PT: Route should start and end with a stop_position"));
				builder.primitives(primitives);
				builder.highlight(highlighted);
				TestError e = builder.build();
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

				Builder builder = TestError.builder(this.test, Severity.WARNING,
						PTAssistantValidatorTest.ERROR_CODE_SPLIT_WAY);
				builder.message(tr("PT: First or last way needs to be split"));
				builder.primitives(primitives);
				builder.highlight(highlighted);
				TestError e = builder.build();
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
				Builder builder = TestError.builder(this.test, Severity.WARNING,
						PTAssistantValidatorTest.ERROR_CODE_SPLIT_WAY);
				builder.message(tr("PT: First or last way needs to be split"));
				builder.primitives(primitives);
				builder.highlight(highlighted);
				TestError e = builder.build();
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

	public void performStopNotServedTest() {
		for (PTStop stop : manager.getPTStops()) {
			Way way = assigner.get(stop);
			if (way == null) {
				createStopError(stop);
			}
		}
	}

	/**
	 * Performs the stop-by-stop test by visiting each segment between two
	 * consecutive stops and checking if the ways between them are correct
	 */
	public void performStopByStopTest() {

		if (manager.getPTStopCount() < 2) {
			return;
		}

		List<OsmPrimitive> lastCreatedBuilderHighlighted = null;

		// Check each route segment:
		for (int i = 1; i < manager.getPTStopCount(); i++) {

			PTStop startStop = manager.getPTStops().get(i - 1);
			PTStop endStop = manager.getPTStops().get(i);

			Way startWay = assigner.get(startStop);
			Way endWay = assigner.get(endStop);
			if (startWay == null || endWay == null || (startWay == endWay && startWay == manager.getLastWay())) {
				continue;
			}

			List<PTWay> segmentWays = manager.getPTWaysBetween(startWay, endWay);

			Node firstNode = findFirstNodeOfRouteSegmentInDirectionOfTravel(segmentWays.get(0));

			if (firstNode == null) {
				// check if this error has just been reported:
				if (wrongSegmentBuilders.isEmpty() && lastCreatedBuilderHighlighted != null && lastCreatedBuilderHighlighted.size() == 1
						&& lastCreatedBuilderHighlighted.get(0) == startWay) {
					// do nothing, this error has already been reported in
					// the previous route segment
				} else {
					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(relation);
					List<OsmPrimitive> highlighted = new ArrayList<>();
					highlighted.add(startWay);
					Builder builder = TestError.builder(this.test, Severity.WARNING,
							PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP);
					builder.primitives(primitives);
					builder.highlight(highlighted);
					PTRouteSegment routeSegment = new PTRouteSegment(startStop, endStop, segmentWays, relation);
					wrongSegmentBuilders.put(builder, routeSegment);
				}
				continue;
			}

			PTWay wronglySortedPtway = existingWaySortingIsWrong(segmentWays.get(0), firstNode,
					segmentWays.get(segmentWays.size() - 1));
			if (wronglySortedPtway == null) { // i.e. if the sorting is correct:
				PTRouteSegment routeSegment = new PTRouteSegment(startStop, endStop, segmentWays, relation);
				addCorrectSegment(routeSegment);
			} else { // i.e. if the sorting is wrong:
				PTRouteSegment routeSegment = new PTRouteSegment(startStop, endStop, segmentWays, relation);
				// TestError error = this.errors.get(this.errors.size() - 1);
				// wrongSegments.put(error, routeSegment);

				List<Relation> primitives = new ArrayList<>(1);
				primitives.add(relation);
				List<OsmPrimitive> highlighted = new ArrayList<>();
				highlighted.addAll(wronglySortedPtway.getWays());
				Builder builder = TestError.builder(this.test, Severity.WARNING,
						PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP);
				builder.primitives(primitives);
				builder.highlight(highlighted);
				lastCreatedBuilderHighlighted = highlighted;
				wrongSegmentBuilders.put(builder, routeSegment);
			}
		}
	}

	/**
	 * Creates a TestError and adds it to the list of errors for a stop that is
	 * not served.
	 *
	 * @param stop
	 *            stop
	 */
	private void createStopError(PTStop stop) {
		List<Relation> primitives = new ArrayList<>(1);
		primitives.add(relation);
		List<OsmPrimitive> highlighted = new ArrayList<>();
		OsmPrimitive stopPrimitive = stop.getPlatform();
		if (stopPrimitive == null) {
			stopPrimitive = stop.getStopPosition();
		}
		highlighted.add(stopPrimitive);
		Builder builder = TestError.builder(this.test, Severity.WARNING,
				PTAssistantValidatorTest.ERROR_CODE_STOP_NOT_SERVED);
		builder.message(tr("PT: Stop not served"));
		builder.primitives(primitives);
		builder.highlight(highlighted);
		TestError e = builder.build();
		this.errors.add(e);
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
		PTWay wayAfterNext = manager.getNextPTWay(nextWay);
		Node[] nextWayEndnodes = nextWay.getEndNodes();
		if ((startWayEndnodes[0] == nextWayEndnodes[0] && startWayEndnodes[1] == nextWayEndnodes[1])
				|| (startWayEndnodes[0] == nextWayEndnodes[1] && startWayEndnodes[1] == nextWayEndnodes[0])) {
			// if this is a split roundabout:
			Node[] wayAfterNextEndnodes = wayAfterNext.getEndNodes();
			if (startWayEndnodes[0] == wayAfterNextEndnodes[0] || startWayEndnodes[0] == wayAfterNextEndnodes[1]) {
				return startWayEndnodes[0];
			}
			if (startWayEndnodes[1] == wayAfterNextEndnodes[0] || startWayEndnodes[1] == wayAfterNextEndnodes[1]) {
				return startWayEndnodes[1];
			}
		}

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
	 *            dead end nodes
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

	/**
	 * Checks if the existing sorting of the given route segment is correct
	 *
	 * @param start
	 *            PTWay assigned to the first stop of the segment
	 * @param startWayPreviousNodeInDirectionOfTravel
	 *            Node if the start way which is furthest away from the rest of
	 *            the route
	 * @param end
	 *            PTWay assigned to the end stop of the segment
	 * @return null if the sorting is correct, or the wrongly sorted PTWay
	 *         otherwise.
	 */
	private PTWay existingWaySortingIsWrong(PTWay start, Node startWayPreviousNodeInDirectionOfTravel, PTWay end) {

		if (start == end) {
			// if both PTStops are on the same PTWay
			// return true;
			return null;
		}

		PTWay current = start;
		Node currentNode = startWayPreviousNodeInDirectionOfTravel;

		while (!current.equals(end)) {
			// "equals" is used here instead of "==" because when the same way
			// is passed multiple times by the bus, the algorithm should stop no
			// matter which of the geometrically equal PTWays it finds

			PTWay nextPTWayAccortingToExistingSorting = manager.getNextPTWay(current);

			// if current contains an unsplit roundabout:
			if (current.containsUnsplitRoundabout()) {
				currentNode = manager.getCommonNode(current, nextPTWayAccortingToExistingSorting);
				if (currentNode == null) {

					return current;

				}
			} else {
				// if this is a regular way, not an unsplit roundabout

				// find the next node in direction of travel (which is part of
				// the PTWay start):
				currentNode = getOppositeEndNode(current, currentNode);

				List<PTWay> nextWaysInDirectionOfTravel = this.findNextPTWaysInDirectionOfTravel(current, currentNode);

				if (!nextWaysInDirectionOfTravel.contains(nextPTWayAccortingToExistingSorting)) {
					return current;

				}
			}

			current = nextPTWayAccortingToExistingSorting;

		}

		return null;
	}

	/**
	 * Will return the same node if the way is an unsplit roundabout
	 *
	 * @param way
	 *            way
	 * @param node
	 *            node
	 * @return the same node if the way is an unsplit roundabout
	 */
	private Node getOppositeEndNode(Way way, Node node) {

		if (node == way.firstNode()) {
			return way.lastNode();
		}

		if (node == way.lastNode()) {
			return way.firstNode();
		}

		return null;
	}

	/**
	 * Does not work correctly for unsplit roundabouts
	 *
	 * @param ptway
	 *            way
	 * @param node
	 *            node
	 * @return node
	 */
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
	 * Finds the next ways for the route stop-by-stop parsing procedure
	 *
	 * @param currentWay
	 *            current way
	 * @param nextNodeInDirectionOfTravel
	 *            next node in direction of travel
	 * @return the next ways for the route stop-by-stop parsing procedure
	 */
	private List<PTWay> findNextPTWaysInDirectionOfTravel(PTWay currentWay, Node nextNodeInDirectionOfTravel) {

		List<PTWay> nextPtways = new ArrayList<>();

		List<PTWay> ptways = manager.getPTWays();

		for (PTWay ptway : ptways) {

			if (ptway != currentWay) {
				for (Way way : ptway.getWays()) {
					if (way.containsNode(nextNodeInDirectionOfTravel)) {
						nextPtways.add(ptway);
					}
				}
			}
		}

		return nextPtways;

	}

	protected static boolean isFixable(TestError testError) {

		/*-
		 * When is an error fixable (outdated)?
		 * - if there is a correct segment
		 * - if it can be fixed by sorting
		 * - if the route is compete even without some ways
		 * - if simple routing closes the gap
		 */

		if (testError.getCode() == PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP) {
			return true;
		}

		return false;

	}

	@SuppressWarnings("unused")
	private static boolean isFixableByUsingCorrectSegment(TestError testError) {
		PTRouteSegment wrongSegment = wrongSegments.get(testError);
		PTRouteSegment correctSegment = null;
		for (PTRouteSegment segment : correctSegments) {
			if (wrongSegment.getFirstStop().equalsStop(segment.getFirstStop())
					&& wrongSegment.getLastStop().equalsStop(segment.getLastStop())) {
				correctSegment = segment;
				break;
			}
		}
		return correctSegment != null;
	}

	@SuppressWarnings("unused")
	private static boolean isFixableBySortingAndRemoval(TestError testError) {
		PTRouteSegment wrongSegment = wrongSegments.get(testError);
		List<List<PTWay>> fixVariants = wrongSegment.getFixVariants();
		if (!fixVariants.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Finds fixes using sorting and removal. 
	 */
	protected void findFixes() {

		for (Builder builder : wrongSegmentBuilders.keySet()) {

			if (wrongSegmentBuilders.get(builder).getRelation() == this.relation) {

				findFix(builder);

			}
		}

	}

	/**
	 * Modifies the error messages of the stop-by-stop test errors depending on how many fixes each of them has.
	 */
	protected static void modifyStopByStopErrorMessages() {

		for (Entry<Builder, PTRouteSegment> entry : SegmentChecker.wrongSegmentBuilders.entrySet()) {

			// change the error code based on the availability of fixes:
			Builder builder = entry.getKey();
			PTRouteSegment wrongSegment = entry.getValue();
			List<PTRouteSegment> correctSegmentsForThisError = new ArrayList<>();
			for (PTRouteSegment segment : correctSegments) {
				if (wrongSegment.getFirstWay().getId() == segment.getFirstWay().getId()
						&& wrongSegment.getLastWay().getId() == segment.getLastWay().getId()) {
					correctSegmentsForThisError.add(segment);
				}
			}

			int numberOfFixes = correctSegmentsForThisError.size();

			if (numberOfFixes == 0) {
				numberOfFixes = wrongSegment.getFixVariants().size();
			}
			if (numberOfFixes == 0) {
				for (PTRouteSegment segment : correctSegments) {
					if (wrongSegment.getFirstStop().equalsStop(segment.getFirstStop())
							&& wrongSegment.getLastStop().equalsStop(segment.getLastStop())) {
						correctSegmentsForThisError.add(segment);
					}
				}
				numberOfFixes = correctSegmentsForThisError.size();
			}

			// change the error message:
			if (numberOfFixes == 0) {
				builder.message(tr("PT: Problem in the route segment with no automatic fix"));
			} else if (numberOfFixes == 1) {
				builder.message(tr("PT: Problem in the route segment with one automatic fix"));
			} else {
				builder.message("PT: Problem in the route segment with several automatic fixes");
			}

		}

	}

	/**
	 * This method assumes that the first and the second ways of the route
	 * segment are correctly connected. If they are not, the error will be
	 * marked as not fixable.
	 *
	 * @param testError
	 *            test error
	 */
	private void findFix(Builder builder) {

		PTRouteSegment wrongSegment = wrongSegmentBuilders.get(builder);
		PTWay startPTWay = wrongSegment.getFirstPTWay();
		PTWay endPTWay = wrongSegment.getLastPTWay();

		Node previousNode = findFirstNodeOfRouteSegmentInDirectionOfTravel(startPTWay);
		if (previousNode == null) {
			return;
		}

		List<List<PTWay>> initialFixes = new ArrayList<>();
		List<PTWay> initialFix = new ArrayList<>();
		initialFix.add(startPTWay);
		initialFixes.add(initialFix);

		List<List<PTWay>> allFixes = findWaysForFix(initialFixes, initialFix, previousNode, endPTWay);
		for (List<PTWay> fix : allFixes) {
			if (!fix.isEmpty() && fix.get(fix.size() - 1).equals(endPTWay)) {
				wrongSegment.addFixVariant(fix);
			}
		}

	}

	/**
	 * Recursive method to parse the route segment
	 *
	 * @param allFixes
	 *            all fixes
	 * @param currentFix
	 *            current fix
	 * @param previousNode
	 *            previous node
	 * @param endWay
	 *            end way
	 * @return list of list of ways
	 */
	private List<List<PTWay>> findWaysForFix(List<List<PTWay>> allFixes, List<PTWay> currentFix, Node previousNode,
			PTWay endWay) {

		PTWay currentWay = currentFix.get(currentFix.size() - 1);
		Node nextNode = getOppositeEndNode(currentWay, previousNode);

		List<PTWay> nextWays = this.findNextPTWaysInDirectionOfTravel(currentWay, nextNode);

		if (nextWays.size() > 1) {
			for (int i = 1; i < nextWays.size(); i++) {
				List<PTWay> newFix = new ArrayList<>();
				newFix.addAll(currentFix);
				newFix.add(nextWays.get(i));
				allFixes.add(newFix);
				if (!nextWays.get(i).equals(endWay) && !currentFix.contains(nextWays.get(i))) {
					allFixes = findWaysForFix(allFixes, newFix, nextNode, endWay);
				}
			}
		}

		if (!nextWays.isEmpty()) {
			boolean contains = currentFix.contains(nextWays.get(0));
			currentFix.add(nextWays.get(0));
			if (!nextWays.get(0).equals(endWay) && !contains) {
				allFixes = findWaysForFix(allFixes, currentFix, nextNode, endWay);
			}
		}

		return allFixes;
	}

	/**
	 * Fixes the error by first searching in the list of correct segments and
	 * then trying to sort and remove existing route relation members
	 *
	 * @param testError
	 *            test error
	 * @return fix command
	 */
	protected static Command fixError(TestError testError) {

		// if fix options for another route are displayed in the pt_assistant
		// layer, clear them:
		((PTAssistantValidatorTest) testError.getTester()).clearFixVariants();

		PTRouteSegment wrongSegment = wrongSegments.get(testError);

		// 1) try to fix by using the correct segment:
		List<PTRouteSegment> correctSegmentsForThisError = new ArrayList<>();
		for (PTRouteSegment segment : correctSegments) {
			if (wrongSegment.getFirstWay().getId() == segment.getFirstWay().getId()
					&& wrongSegment.getLastWay().getId() == segment.getLastWay().getId()) {
				correctSegmentsForThisError.add(segment);
			}
		}

		// if no correct segment found, apply less strict criteria to look for
		// one:
		if (correctSegmentsForThisError.isEmpty() && wrongSegment.getFixVariants().isEmpty()) {
			for (PTRouteSegment segment : correctSegments) {
				if (wrongSegment.getFirstStop().equalsStop(segment.getFirstStop())
						&& wrongSegment.getLastStop().equalsStop(segment.getLastStop())) {
					correctSegmentsForThisError.add(segment);
				}
			}
			if (!correctSegmentsForThisError.isEmpty()) {
				// display the notification:
				if (SwingUtilities.isEventDispatchThread()) {
					Notification notification = new Notification(
							tr("Warning: the diplayed fix variants are based on less strict criteria"));
					notification.show();
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							Notification notification = new Notification(
									tr("Warning: the diplayed fix variants are based on less strict criteria"));
							notification.show();
						}
					});
				}
			}
		}

		if (!correctSegmentsForThisError.isEmpty()) {

			if (correctSegmentsForThisError.size() > 1) {
				List<List<PTWay>> fixVariants = new ArrayList<>();
				for (PTRouteSegment segment : correctSegmentsForThisError) {
					fixVariants.add(segment.getPTWays());
				}
				displayFixVariants(fixVariants, testError);
				return null;
			}

			PTAssistantPlugin.setLastFix(correctSegmentsForThisError.get(0));
			return carryOutSingleFix(testError, correctSegmentsForThisError.get(0).getPTWays());

		} else if (!wrongSegment.getFixVariants().isEmpty()) {
			// 2) try to fix using the sorting and removal of existing ways
			// of the wrong segment:
			if (wrongSegment.getFixVariants().size() > 1) {
				displayFixVariants(wrongSegment.getFixVariants(), testError);
				return null;
			}

			PTAssistantPlugin.setLastFix(new PTRouteSegment(wrongSegment.getFirstStop(), wrongSegment.getLastStop(),
					wrongSegment.getFixVariants().get(0), (Relation) testError.getPrimitives().iterator().next()));
			return carryOutSingleFix(testError, wrongSegment.getFixVariants().get(0));
		}

		// if there is no fix:
		return fixErrorByZooming(testError);

	}

	/**
	 * This is largely a copy of the displayFixVariants() method, adapted for
	 * use with the key listener
	 *
	 * @param fixVariants
	 *            fix variants
	 * @param testError
	 *            test error
	 */
	private static void displayFixVariants(List<List<PTWay>> fixVariants, TestError testError) {
		// find the letters of the fix variants:
		char alphabet = 'A';
		final List<Character> allowedCharacters = new ArrayList<>();
		for (int i = 0; i < fixVariants.size(); i++) {
			allowedCharacters.add(alphabet);
			alphabet++;
		}

		// zoom to problem:
		final Collection<OsmPrimitive> waysToZoom = new ArrayList<>();
		for (Object highlightedPrimitive : testError.getHighlighted()) {
			waysToZoom.add((OsmPrimitive) highlightedPrimitive);
		}
		if (SwingUtilities.isEventDispatchThread()) {
			AutoScaleAction.zoomTo(waysToZoom);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					AutoScaleAction.zoomTo(waysToZoom);
				}
			});
		}

		// display the fix variants:
		final PTAssistantValidatorTest test = (PTAssistantValidatorTest) testError.getTester();
		test.addFixVariants(fixVariants);
		PTAssistantLayer.getLayer().repaint((Relation) testError.getPrimitives().iterator().next());

		// prepare the variables for the key listener:
		final TestError testErrorParameter = testError;

		// // add the key listener:
		Main.map.mapView.requestFocus();
		Main.map.mapView.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			public void keyPressed(KeyEvent e) {
				Character typedKey = e.getKeyChar();
				Character typedKeyUpperCase = typedKey.toString().toUpperCase().toCharArray()[0];
				if (allowedCharacters.contains(typedKeyUpperCase)) {
					Main.map.mapView.removeKeyListener(this);
					List<PTWay> selectedFix = test.getFixVariant(typedKeyUpperCase);
					test.clearFixVariants();
					carryOutSelectedFix(testErrorParameter, selectedFix);
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					Main.map.mapView.removeKeyListener(this);
					test.clearFixVariants();
				}
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});

		// display the notification:
		if (SwingUtilities.isEventDispatchThread()) {
			Notification notification = new Notification(
					tr("Type letter to select the fix variant or press Escape for no fix"));
			notification.show();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Notification notification = new Notification(
							tr("Type letter to select the fix variant or press Escape for no fix"));
					notification.show();
				}
			});
		}
	}

	/**
	 * Carries out the fix (i.e. modifies the route) after the user has picked
	 * the fix from several fix variants.
	 *
	 * @param testError
	 *            test error to be fixed
	 * @param fix
	 *            the fix variant to be adopted
	 */
	private static void carryOutSelectedFix(TestError testError, List<PTWay> fix) {
		// modify the route:
		Relation originalRelation = (Relation) testError.getPrimitives().iterator().next();
		Relation modifiedRelation = new Relation(originalRelation);
		modifiedRelation.setMembers(getModifiedRelationMembers(testError, fix));
		ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);
		Main.main.undoRedo.addNoRedraw(changeCommand);
		Main.main.undoRedo.afterAdd();
		PTRouteSegment wrongSegment = wrongSegments.get(testError);
		wrongSegments.remove(testError);
		wrongSegment.setPTWays(fix);
		addCorrectSegment(wrongSegment);
		PTAssistantPlugin.setLastFixNoGui(wrongSegment);

		// get ways for the fix:
		List<Way> primitives = new ArrayList<>();
		for (PTWay ptway : fix) {
			primitives.addAll(ptway.getWays());
		}

		// get layer:
		OsmDataLayer layer = null;
		List<OsmDataLayer> listOfLayers = Main.getLayerManager().getLayersOfType(OsmDataLayer.class);
		for (OsmDataLayer osmDataLayer : listOfLayers) {
			if (osmDataLayer.data == originalRelation.getDataSet()) {
				layer = osmDataLayer;
				break;
			}
		}

		// create editor:
		GenericRelationEditor editor = (GenericRelationEditor) RelationEditor.getEditor(layer, originalRelation,
				originalRelation.getMembersFor(primitives));

		// open editor:
		editor.setVisible(true);

	}

	/**
	 * Carries out the fix (i.e. modifies the route) when there is only one fix
	 * variant.
	 *
	 * @param testError
	 *            test error
	 * @param fix
	 *            fix
	 */
	private static Command carryOutSingleFix(TestError testError, List<PTWay> fix) {
		// Zoom to the problematic ways:
		final Collection<OsmPrimitive> waysToZoom = new ArrayList<>();
		for (Object highlightedPrimitive : testError.getHighlighted()) {
			waysToZoom.add((OsmPrimitive) highlightedPrimitive);
		}
		if (SwingUtilities.isEventDispatchThread()) {
			AutoScaleAction.zoomTo(waysToZoom);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					AutoScaleAction.zoomTo(waysToZoom);
				}
			});
		}

		// wait:
		synchronized (SegmentChecker.class) {
			try {
				SegmentChecker.class.wait(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// modify the route:
		Relation originalRelation = (Relation) testError.getPrimitives().iterator().next();
		Relation modifiedRelation = new Relation(originalRelation);
		modifiedRelation.setMembers(getModifiedRelationMembers(testError, fix));
		wrongSegments.remove(testError);
		ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);
		return changeCommand;
	}

	/**
	 * Returns a list of the modified relation members. This list can be used by
	 * the calling method (relation.setMemers()) to modify the modify the route
	 * relation. The route relation is not modified by this method. The lists of
	 * wrong and correct segments are not updated.
	 *
	 * @param testError
	 *            test error to be fixed
	 * @param fix
	 *            the fix variant to be adopted
	 * @return List of modified relation members to be applied to the route
	 *         relation
	 */
	private static List<RelationMember> getModifiedRelationMembers(TestError testError, List<PTWay> fix) {
		PTRouteSegment wrongSegment = wrongSegments.get(testError);
		Relation originalRelation = (Relation) testError.getPrimitives().iterator().next();

		// copy stops first:
		List<RelationMember> modifiedRelationMembers = listStopMembers(originalRelation);

		// copy PTWays last:
		List<RelationMember> waysOfOriginalRelation = listNotStopMembers(originalRelation);
		for (int i = 0; i < waysOfOriginalRelation.size(); i++) {
			if (waysOfOriginalRelation.get(i).getWay() == wrongSegment.getPTWays().get(0).getWays().get(0)) {
				modifiedRelationMembers.addAll(fix);
				i = i + wrongSegment.getPTWays().size() - 1;
			} else {
				modifiedRelationMembers.add(waysOfOriginalRelation.get(i));
			}
		}

		return modifiedRelationMembers;
	}

	public static void carryOutRepeatLastFix(PTRouteSegment segment) {

		List<TestError> wrongSegmentsToRemove = new ArrayList<>();

		// find all wrong ways that have the same segment:
		for (TestError testError : wrongSegments.keySet()) {
			PTRouteSegment wrongSegment = wrongSegments.get(testError);
			if (wrongSegment.getFirstWay() == segment.getFirstWay()
					&& wrongSegment.getLastWay() == segment.getLastWay()) {
				// modify the route:
				Relation originalRelation = wrongSegment.getRelation();
				Relation modifiedRelation = new Relation(originalRelation);
				modifiedRelation.setMembers(getModifiedRelationMembers(testError, segment.getPTWays()));
				ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);
				Main.main.undoRedo.addNoRedraw(changeCommand);
				Main.main.undoRedo.afterAdd();
				wrongSegmentsToRemove.add(testError);
			}
		}

		// update the errors displayed in the validator dialog:
		List<TestError> modifiedValidatorTestErrors = new ArrayList<>();
		for (TestError validatorTestError : Main.map.validatorDialog.tree.getErrors()) {
			if (!wrongSegmentsToRemove.contains(validatorTestError)) {
				modifiedValidatorTestErrors.add(validatorTestError);
			}
		}
		Main.map.validatorDialog.tree.setErrors(modifiedValidatorTestErrors);

		// update wrong segments:
		for (TestError testError : wrongSegmentsToRemove) {
			wrongSegments.remove(testError);
		}

	}

	/**
	 * Resets the static list variables (used for unit tests and in Test.startTest() method)
	 */
	protected static void reset() {
		correctSegments.clear();
		wrongSegments.clear();
		wrongSegmentBuilders.clear();
	}

}
