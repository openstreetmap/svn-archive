package org.openstreetmap.josm.plugins.graphview.core.graph;

import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.property.GraphEdgePropertyType;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;

/**
 * directed connection between two nodes in a {@link WayGraph}
 */
public interface GraphEdge {

	/** returns the node this edge starts at; != null */
	GraphNode getStartNode();

	/** returns the node this edge leads to; != null */
	GraphNode getTargetNode();

	/** returns all property types for which property values are available */
	Collection<GraphEdgePropertyType<?>> getAvailableProperties();
	
	/** TODO */
	<V> V getPropertyValue(GraphEdgePropertyType<V> property);
	
}
