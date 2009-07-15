package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;

/**
 * default strategy to place nodes.
 * Will move every node whose SegmentNode is connected to more than two segments
 * at most 1/3 of segment length away from the original position.
 */
public class DefaultNodePositioner implements NodePositioner {

	public LatLonCoords getPosition(GraphNode node) {

		SegmentNode segmentNode = node.getSegmentNode();

		if (2 >= segmentNode.getInboundSegments().size()
				+ segmentNode.getOutboundSegments().size() ) {

			return new LatLonCoords(
					node.getSegmentNode().getLat(),
					node.getSegmentNode().getLon());

		} else {

			SegmentNode node1 = node.getSegment().getNode1();
			SegmentNode node2 = node.getSegment().getNode2();

			assert segmentNode == node1 || segmentNode == node2;

			LatLonCoords result;

			if (segmentNode == node1) {
				result = new LatLonCoords(
						(2 * node1.getLat() + node2.getLat()) / 3,
						(2 * node1.getLon() + node2.getLon()) / 3);
			} else {
				result = new LatLonCoords(
						(node1.getLat() + 2 * node2.getLat()) / 3,
						(node1.getLon() + 2 * node2.getLon()) / 3);
			}

			return result;
		}
	}

}
