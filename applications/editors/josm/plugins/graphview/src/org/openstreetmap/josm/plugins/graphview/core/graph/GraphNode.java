package org.openstreetmap.josm.plugins.graphview.core.graph;

import java.util.Collection;

import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;

/**
 * node in a {@link WayGraph};
 * is based on one Segment and one of its SegmentNodes and describes the state
 * of being on the segment immediately next to the node.
 */
public interface GraphNode {

	/**
	 * returns all edges that lead to this GraphNode; != null
	 */
	public Collection<GraphEdge> getInboundEdges();

	/**
	 * returns all edges that start at this GraphNode; != null
	 */
	public Collection<GraphEdge> getOutboundEdges();

	/**
	 * returns the SegmentNode this GraphNode is based on
	 *
	 * @return  SegmentNode, must be one of the nodes of the Segment returned
	 *          by {@link #getSegment()}; != null
	 */
	public SegmentNode getSegmentNode();

	/**
	 * returns the Segment this GraphNode is based on
	 *
	 * @return  Segment; != null
	 */
	public Segment getSegment();

}
