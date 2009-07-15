package org.openstreetmap.josm.plugins.graphview.core.transition;

import java.util.Collection;

/**
 * node in a {@link TransitionStructure}
 */
public interface SegmentNode extends TransitionStructureElement {

	/** returns the node's latitude */
	public double getLat();

	/** returns the node's longitude */
	public double getLon();

	/** returns all segments that end at this node */
	Collection<Segment> getInboundSegments();

	/** returns all segments that start at this node */
	Collection<Segment> getOutboundSegments();

}
