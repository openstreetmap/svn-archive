package org.openstreetmap.josm.plugins.graphview.core.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.openstreetmap.josm.plugins.graphview.core.transition.Restriction;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;

/**
 * group of nodes and/or segments that will be evaluated independently from other groups
 */
abstract class EvaluationGroup {

	protected boolean evaluated = false;

	/**
	 * array of sequences.
	 * First index is inbound segment/start node index,
	 * second index is outbound segment/target node index.
	 * Will contain the segment sequence after evaluation or null if none exists.
	 */
	protected List<Segment>[][] segmentSequences;

	private static final List<Segment> EMPTY_SEGMENT_LIST =
		Collections.unmodifiableList(new ArrayList<Segment>(0));

	private static final List<Restriction> EMPTY_RESTRICTION_LIST =
		Collections.unmodifiableList(new ArrayList<Restriction>(0));

	private static class State {
		SegmentNode currentNode;
		Set<SegmentNode> visitedNodes;
		Collection<Restriction> activeRestrictions;
		List<Segment> segmentHistory;
	}


	/**
	 * tries to find a legal sequence of segments between two segment nodes.
	 *
	 * @return  list of segments if connection is possible, null otherwise.
	 */
	protected List<Segment> findSegmentSequence(
			SegmentNode firstNode, SegmentNode lastNode,
			Collection<Restriction> restrictions) {

		return findSegmentSequence(firstNode, lastNode, restrictions,
				EMPTY_RESTRICTION_LIST, EMPTY_RESTRICTION_LIST);

	}

	/**
	 * tries to find a legal sequence of segments between two segments.
	 *
	 * @return  list of segments if connection is possible, null otherwise.
	 *          The list does NOT include firstSegment and lastSegment,
	 *          but they are considered for restrictions.
	 */
	protected List<Segment> findSegmentSequence(
			Segment firstSegment, Segment lastSegment,
			Collection<Restriction> restrictions) {

		if (firstSegment == lastSegment) {

			return EMPTY_SEGMENT_LIST;

		} else {

			Collection<Restriction> initiallyActiveRestrictions =
				activeRestrictionsAfterSegment(firstSegment, EMPTY_RESTRICTION_LIST, restrictions);

			Collection<Restriction> restrictionsForbiddenAtLastNode = new HashSet<Restriction>();
			for (Restriction restriction : restrictions) {
				if (restriction.getTos().contains(lastSegment)) {
					restrictionsForbiddenAtLastNode.add(restriction);
				}
			}

			return findSegmentSequence(
					firstSegment.getNode2(), lastSegment.getNode1(), restrictions,
					initiallyActiveRestrictions, restrictionsForbiddenAtLastNode);
		}

	}

	/**
	 * tries to find a legal sequence of segments between two segment nodes.
	 *
	 * @param restrictions  all restrictions that have to be taken into account
	 * @param initiallyActiveRestrictions  restrictions that are already active at firstNode
	 * @param restrictionsForbiddenAtLastNode  restrictions that must NOT be active at lastNode
	 * @return  list of segments if connection is possible, null otherwise.
	 */
	private List<Segment> findSegmentSequence(
			SegmentNode firstNode, SegmentNode lastNode,
			Collection<Restriction> restrictions,
			Collection<Restriction> initiallyActiveRestrictions,
			Collection<Restriction> restrictionsForbiddenAtLastNode) {

		if (firstNode == lastNode
				&& !shareElement(initiallyActiveRestrictions, restrictionsForbiddenAtLastNode)) {
			return EMPTY_SEGMENT_LIST;
		}

		Queue<State> stateQueue = new LinkedList<State>();
		stateQueue.add(createStartingState(firstNode, initiallyActiveRestrictions));

		/* search for a possible segment sequence */

		while (stateQueue.size() > 0) {

			State state = stateQueue.poll();

			Collection<State> subsequentStates = createSubsequentStates(state, restrictions);

			for (State subsequentState : subsequentStates) {
				if (subsequentState.currentNode == lastNode
						&& !shareElement(subsequentState.activeRestrictions,
								restrictionsForbiddenAtLastNode)) {
					return subsequentState.segmentHistory;
				}
			}

			stateQueue.addAll(subsequentStates);

		}

		return null;
	}

	private static State createStartingState(SegmentNode firstNode,
			Collection<Restriction> initiallyActiveRestrictions) {

		State startingState = new State();
		startingState.currentNode = firstNode;
		startingState.activeRestrictions = initiallyActiveRestrictions;
		startingState.segmentHistory = EMPTY_SEGMENT_LIST;
		startingState.visitedNodes = new HashSet<SegmentNode>();
		startingState.visitedNodes.add(firstNode);

		return startingState;
	}

	private List<State> createSubsequentStates(State state, Collection<Restriction> allRestrictions) {

		List<State> subsequentStates = new ArrayList<State>();

		for (Segment segment : state.currentNode.getOutboundSegments()) {

			if (isUsableSegment(segment) &&
					isLegalSegment(segment, state.activeRestrictions)) {

				State newState = new State();

				newState.activeRestrictions = activeRestrictionsAfterSegment(
						segment, state.activeRestrictions, allRestrictions);

				newState.segmentHistory = new ArrayList<Segment>(state.segmentHistory.size() + 1);
				newState.segmentHistory.addAll(state.segmentHistory);
				newState.segmentHistory.add(segment);

				newState.currentNode = segment.getNode2();

				newState.visitedNodes = new HashSet<SegmentNode>(state.visitedNodes);
				newState.visitedNodes.add(newState.currentNode);

				/* add state to queue,
				 * but avoid cycles as well as leaving the node set
				 */

				if (!state.visitedNodes.contains(newState.currentNode)
						&& isUsableNode(newState.currentNode)) {

					subsequentStates.add(newState);

				}
			}
		}

		return subsequentStates;
	}

	/**
	 * returns all restrictions from a collection that have a segment as from member
	 * @return  segment list; != null; must not be modified.
	 *          May throw an exception when modifying is attempted.
	 */
	private static List<Restriction> getRestrictionsStartedBySegment(
			Collection<Restriction> restrictions, Segment segment) {

		List<Restriction> result = EMPTY_RESTRICTION_LIST;
		for (Restriction restriction : restrictions) {
			if (restriction.getFrom() == segment) {
				if (result == EMPTY_RESTRICTION_LIST) {
					result = new ArrayList<Restriction>(restrictions.size());
				}
				result.add(restriction);
			}
		}

		return result;
	}

	private static Collection<Restriction> activeRestrictionsAfterSegment(Segment segment,
			Collection<Restriction> activeRestrictionsBeforeSegment,
			Collection<Restriction> allRestrictions) {

		Collection<Restriction> result = EMPTY_RESTRICTION_LIST;

		for (Restriction restriction : activeRestrictionsBeforeSegment) {
			if (restriction.getVias().contains(segment)) {
				if (result == EMPTY_RESTRICTION_LIST) {
					result = new ArrayList<Restriction>(allRestrictions.size());
				}
				result.add(restriction);
			}
		}

		Collection<Restriction> newRestrictions =
			getRestrictionsStartedBySegment(allRestrictions, segment);

		if (newRestrictions.size() > 0) {
			if (result == EMPTY_RESTRICTION_LIST) {
				result = newRestrictions;
			} else {
				result.addAll(newRestrictions);
			}
		}

		return result;
	}

	private static boolean isLegalSegment(
			Segment segment, Collection<Restriction> activeRestrictions) {

		for (Restriction restriction : activeRestrictions) {
			if (restriction.getTos().contains(segment)) {
				return false;
			}
		}

		return true;
	}

	/** returns true iff at least one element is contained in both collections */
	protected static boolean shareElement(
			Collection<?> collection1, Collection<?> collection2) {
		for (Object element : collection1) {
			if (collection2.contains(element)) {
				return true;
			}
		}
		return false;
	}

	public final void evaluate(Collection<Restriction> restrictions) {

		if (evaluated) { return; }

		evaluateImpl(restrictions);

		evaluated = true;
	}

	/**
	 * finds in- and outbound segments (if necessary) and segment sequences.
	 * After calling this method, the group must be correctly evaluated
	 * (see {@link #isCorrectlyEvaluated()}).
	 *
	 * @param restrictions  restrictions that are used when determining possible connections,
	 *                      will not be modified; != null
	 */
	abstract protected void evaluateImpl(Collection<Restriction> restrictions);

	/**
	 * returns whether a node can be used while finding a segment sequence
	 * @param node  node to check; != null
	 */
	abstract protected boolean isUsableNode(SegmentNode node);

	/**
	 * returns whether a segment can be used while finding a segment sequence
	 * @param segment  segment to check; != null
	 */
	abstract protected boolean isUsableSegment(Segment segment);

}