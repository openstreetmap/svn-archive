package org.openstreetmap.josm.plugins.graphview.core.transition;

/**
 * connection between two {@link SegmentNode}s in a {@link TransitionStructure}
 */
public interface Segment extends TransitionStructureElement {

	/**
	 * returns the node this segment starts at; != null
	 */
	public SegmentNode getNode1();

	/**
	 * returns the node this segment leads to; != null
	 */
	public SegmentNode getNode2();

}
