package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;

public class NonMovingNodePositioner implements NodePositioner {

	public LatLonCoords getPosition(GraphNode node) {
		return new LatLonCoords(
				node.getSegmentNode().getLat(),
				node.getSegmentNode().getLon());
	}

}
