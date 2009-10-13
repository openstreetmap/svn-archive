package org.openstreetmap.josm.plugins.graphview.core.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.plugins.graphview.core.transition.Restriction;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;

/**
 * evaluation group that is based on segments and connects the node-based
 * {@link JunctionEvaluationGroup}s.
 */
public class ConnectorEvaluationGroup extends EvaluationGroup {

	private final Set<Segment> segments;
	private final List<SegmentNode> borderNodes;

	/**
	 * @param segments     set of Segments, must not be modified
	 *                     after being used as constructor parameter; != null
	 * @param borderNodes  nodes that are used as starting/target nodes for sequences; != null
	 */
	public ConnectorEvaluationGroup(Set<Segment> segments, Set<SegmentNode> borderNodes) {
		assert segments != null && borderNodes != null;

		this.segments = segments;
		this.borderNodes = new ArrayList<SegmentNode>(borderNodes);
	}

	/**
	 * returns all nodes that can be used as start/target nodes
	 * @return  border nodes; != null
	 */
	public Collection<SegmentNode> getBorderNodes() {
		return borderNodes;
	}

	/**
	 * returns all segments in the group
	 * @return  segment set; != null
	 */
	public Set<Segment> getSegments() {
		return segments;
	}

	/**
	 * returns a segment sequence that runs from an inbound to an outbound
	 * segment or null if no connection is possible.
	 * {@link EvaluationGroup#evaluate(Collection)} needs be called before this method.
	 *
	 * @param  startNode   start of the potential sequence; must be border node; != null
	 * @param  targetNode  target of the potential sequence; must be border node; != null
	 * @return             sequence of segments or null
	 */
	public List<Segment> getSegmentSequence(SegmentNode startNode, SegmentNode targetNode) {
		assert startNode != null && borderNodes.contains(startNode);
		assert targetNode != null && borderNodes.contains(targetNode);

		if (!evaluated) { throw new IllegalStateException("group not yet evaluated"); }

		int inboundIndex = borderNodes.indexOf(startNode);
		int outboundIndex = borderNodes.indexOf(targetNode);

		return segmentSequences[inboundIndex][outboundIndex];
	}

	@Override
	protected void evaluateImpl(Collection<Restriction> restrictions) {

		/* find segment sequences from inbound to outbound segments */

		@SuppressWarnings("unchecked") //cannot create generic array without cast
		List<Segment>[][] sequenceArray = new List[borderNodes.size()][borderNodes.size()];

		for (int startIndex = 0; startIndex < borderNodes.size(); startIndex ++) {
			for (int targetIndex = 0; targetIndex < borderNodes.size(); targetIndex ++) {

				List<Segment> sequence =
					findSegmentSequence(borderNodes.get(startIndex),
							borderNodes.get(targetIndex), restrictions);

				sequenceArray[startIndex][targetIndex] = sequence;

			}
		}

		segmentSequences = sequenceArray;
	}

	@Override
	protected boolean isUsableNode(SegmentNode node) {
		return shareElement(segments, node.getInboundSegments())
		|| shareElement(segments, node.getOutboundSegments());
	}

	@Override
	protected boolean isUsableSegment(Segment segment) {
		return segments.contains(segment);
	}

	@Override
	public String toString() {
		return "ConnectorEG " + segments;
	}

}