package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import java.awt.Color;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;

/**
 * scheme for coloring edges' segments according to some criteria
 */
public interface ColorScheme {

	/**
	 * returns the color to be used for a node in a WayGraph
	 * @param edge  GraphNode to determine the color for; != null
	 */
	public Color getNodeColor(GraphNode node);

	/**
	 * returns the color to be used for an edge in a WayGraph
	 * @param segment  segment to determine the color for; != null
	 */
	public Color getSegmentColor(Segment segment);

}