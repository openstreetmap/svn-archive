package org.openstreetmap.josm.plugins.graphview.core.util;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.graph.WayGraph;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;

/**
 * utility class for calculating information about {@link WayGraph}s
 */
public final class GraphUtil {

	/** prevents instantiation */
	private GraphUtil() { }

	/**
	 * checks whether a node is an "end node"
	 * (a node whose {@link SegmentNode} is connected to at most one other {@link SegmentNode})
	 */
	public static final boolean isEndNode(GraphNode node) {

		SegmentNode ownSegmentNode = node.getSegmentNode();

		SegmentNode connectedNode = null;

		for (Segment inboundSegment : node.getSegmentNode().getInboundSegments()) {
			SegmentNode otherSegmentNode = inboundSegment.getNode1();
			if (otherSegmentNode != ownSegmentNode) {
				if (connectedNode == null) {
					connectedNode = otherSegmentNode;
				} else if (connectedNode != otherSegmentNode) {
					return false;
				}
			}
		}

		for (Segment outboundSegment : node.getSegmentNode().getOutboundSegments()) {
			SegmentNode otherSegmentNode = outboundSegment.getNode2();
			if (otherSegmentNode != ownSegmentNode) {
				if (connectedNode == null) {
					connectedNode = otherSegmentNode;
				} else if (connectedNode != otherSegmentNode) {
					return false;
				}
			}
		}

		return true;

	}

}
