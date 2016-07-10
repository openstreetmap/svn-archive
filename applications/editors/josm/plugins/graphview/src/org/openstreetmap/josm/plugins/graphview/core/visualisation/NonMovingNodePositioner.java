// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;

public class NonMovingNodePositioner implements NodePositioner {

    @Override
    public LatLonCoords getPosition(GraphNode node) {
        return new LatLonCoords(
                node.getSegmentNode().getLat(),
                node.getSegmentNode().getLon());
    }

}
