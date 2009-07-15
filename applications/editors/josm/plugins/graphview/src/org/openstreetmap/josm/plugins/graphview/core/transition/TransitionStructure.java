package org.openstreetmap.josm.plugins.graphview.core.transition;

import java.util.Collection;

/**
 * graph-like structure for transition between OSM node/way/relation representation
 * and the WayGraph. It consists of Nodes, Segments, and Restrictions.
 */
public interface TransitionStructure {

	public Collection<SegmentNode> getNodes();
	public Collection<Segment> getSegments();
	public Collection<Restriction> getRestrictions();

	/**
	 * adds an observer.
	 * Does nothing if the parameter is already an observer of this TransitionStructure.
	 *
	 * @param observer  observer object, != null
	 */
	public void addObserver(TransitionStructureObserver observer);

	/**
	 * deletes an observer that has been added using {@link #addObserver(TransitionStructureObserver)}.
	 * Does nothing if the parameter isn't currently an observer of this TransitionStructure.
	 *
	 * @param observer  observer object, != null
	 */
	public void deleteObserver(TransitionStructureObserver observer);

}
