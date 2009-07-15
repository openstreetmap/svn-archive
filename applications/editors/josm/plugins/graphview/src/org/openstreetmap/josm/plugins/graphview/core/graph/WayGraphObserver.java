package org.openstreetmap.josm.plugins.graphview.core.graph;

/**
 * observer that will be informed about changes in a WayGraph
 * if it has been registered using {@link WayGraph#addObserver(WayGraphObserver)}
 */
public interface WayGraphObserver {

	/**
	 * informs this observer about changes in an observed graph
	 * @param wayGraph  observed graph that has changed; != null
	 */
	public void update(WayGraph wayGraph);

}
