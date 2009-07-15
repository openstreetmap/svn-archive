package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.graph.WayGraph;

/**
 * strategy for arranging a {@link WayGraph}'s nodes
 */
public interface NodePositioner {

	LatLonCoords getPosition(GraphNode node);

}
