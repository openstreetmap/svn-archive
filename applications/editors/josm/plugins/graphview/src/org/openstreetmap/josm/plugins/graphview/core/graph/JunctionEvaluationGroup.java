package org.openstreetmap.josm.plugins.graphview.core.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.plugins.graphview.core.transition.Restriction;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;

/**
 * group of nodes that will be evaluated independently from other groups
 */
public class JunctionEvaluationGroup extends EvaluationGroup {

	private final Set<SegmentNode> segmentNodes;

	protected List<Segment> inboundSegments;
	protected List<Segment> outboundSegments;

	/**
	 * @param segmentNodes  set of SegmentNodes, must not be modified
	 *                      after being used as constructor parameter; != null
	 */
	public JunctionEvaluationGroup(Set<SegmentNode> segmentNodes) {
		assert segmentNodes != null;
		this.segmentNodes = segmentNodes;
	}

	/**
	 * returns all segments that can be used to enter this group.
	 * {@link #evaluate(Iterable)} needs be called before this method.
	 *
	 * @return  segment collection; != null
	 */
	public Collection<Segment> getInboundSegments() {
		if (!evaluated) { throw new IllegalStateException("group not yet evaluated"); }
		return inboundSegments;
	}

	/**
	 * returns all segments that can be used to leave this group.
	 * {@link #evaluate(Iterable)} needs be called before this method.
	 *
	 * @return  segment collection; != null
	 */
	public Collection<Segment> getOutboundSegments() {
		if (!evaluated) { throw new IllegalStateException("group not yet evaluated"); }
		return outboundSegments;
	}

	/**
	 * returns a segment sequence that runs from an inbound to an outbound
	 * segment or null if no connection is possible.
	 * {@link EvaluationGroup#evaluate(Collection)} needs be called before this method.
	 *
	 * @param  inboundSegment  start of the potential sequence;
	 *                         must be inbound segment; != null
	 * @param  outboundSegment target of the potential sequence;
	 *                         must be outbound segment; != null
	 * @return  sequence of segments or null
	 */
	public List<Segment> getSegmentSequence(Segment inboundSegment, Segment outboundSegment) {
		assert inboundSegment != null && inboundSegments.contains(inboundSegment);
		assert outboundSegment != null && outboundSegments.contains(outboundSegment);

		if (!evaluated) { throw new IllegalStateException("group not yet evaluated"); }

		int inboundIndex = inboundSegments.indexOf(inboundSegment);
		int outboundIndex = outboundSegments.indexOf(outboundSegment);

		return segmentSequences[inboundIndex][outboundIndex];
	}

	@Override
	protected void evaluateImpl(final Collection<Restriction> restrictions) {

		assert restrictions != null;

		/* find inbound and outbound segments. An inbound segment is a segment whose target
		 * is in the set and whose start node isn't (analogous for outbound segments)       */

		inboundSegments = new ArrayList<Segment>();
		outboundSegments = new ArrayList<Segment>();

		for (SegmentNode segmentNode : segmentNodes) {
			for (Segment segment : segmentNode.getInboundSegments()) {
				if (!segmentNodes.contains(segment.getNode1())) {
					inboundSegments.add(segment);
				}
			}
			for (Segment segment : segmentNode.getOutboundSegments()) {
				if (!segmentNodes.contains(segment.getNode2())) {
					outboundSegments.add(segment);
				}
			}
		}

		/* find segment sequences from inbound to outbound segments */

		@SuppressWarnings("unchecked") //cannot create generic array without cast
		List<Segment>[][] sequenceArray = new List[inboundSegments.size()][outboundSegments.size()];

		for (int inboundIndex = 0; inboundIndex < inboundSegments.size(); inboundIndex ++) {
			for (int outboundIndex = 0; outboundIndex < outboundSegments.size(); outboundIndex ++) {

				List<Segment> sequence =
					findSegmentSequence(inboundSegments.get(inboundIndex),
							outboundSegments.get(outboundIndex), restrictions);

				sequenceArray[inboundIndex][outboundIndex] = sequence;

			}
		}

		segmentSequences = sequenceArray;

	}

	@Override
	protected boolean isUsableNode(SegmentNode node) {
		return segmentNodes.contains(node);
	}

	@Override
	protected boolean isUsableSegment(Segment segment) {
		return segmentNodes.contains(segment.getNode1())
		&& segmentNodes.contains(segment.getNode2());
	}

	@Override
	public String toString() {
		return "JunctionEG " + segmentNodes;
	}
}