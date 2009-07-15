package org.openstreetmap.josm.plugins.graphview.core.graph;

import java.util.Collection;

/**
 * graph representing OSM ways purely as nodes and directed edges
 */
public interface WayGraph {

	Collection<GraphNode> getNodes();
	Collection<GraphEdge> getEdges();

	/**
	 * adds an observer.
	 * Does nothing if the parameter is already an observer of this WayGraph.
	 *
	 * @param observer  observer object, != null
	 */
	public void addObserver(WayGraphObserver observer);

	/**
	 * deletes an observer that has been added using {@link #addObserver(WayGraphObserver)}.
	 * Does nothing if the parameter isn't currently an observer of this WayGraph.
	 *
	 * @param observer  observer object, != null
	 */
	public void deleteObserver(WayGraphObserver observer);

}
