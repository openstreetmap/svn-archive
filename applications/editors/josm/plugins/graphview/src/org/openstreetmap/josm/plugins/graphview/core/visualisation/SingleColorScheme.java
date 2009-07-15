package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import java.awt.Color;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;

/**
 * scheme giving the same color to all segments and the same color to all nodes
 * (but possibly different colors for one of them)
 */
public class SingleColorScheme implements ColorScheme {

	private final Color nodeColor;
	private final Color segmentColor;

	public SingleColorScheme(Color nodeColor, Color segmentColor) {
		this.nodeColor = nodeColor;
		this.segmentColor = segmentColor;
	}

	public Color getNodeColor(GraphNode node) {
		return nodeColor;
	}

	public Color getSegmentColor(Segment segment) {
		return segmentColor;
	}

}
