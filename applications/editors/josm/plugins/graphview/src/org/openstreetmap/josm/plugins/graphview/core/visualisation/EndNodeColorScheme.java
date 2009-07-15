package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import java.awt.Color;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.util.GraphUtil;

/**
 * color scheme that highlights end nodes
 */
public class EndNodeColorScheme implements ColorScheme {

	private final Color nodeColor;
	private final Color endNodeColor;
	private final Color segmentColor;

	public EndNodeColorScheme(Color nodeColor, Color endNodeColor, Color segmentColor) {
		this.nodeColor = nodeColor;
		this.endNodeColor = endNodeColor;
		this.segmentColor = segmentColor;
	}

	public Color getNodeColor(GraphNode node) {
		return GraphUtil.isEndNode(node) ? endNodeColor : nodeColor;
	}

	public Color getSegmentColor(Segment segment) {
		return segmentColor;
	}

}
