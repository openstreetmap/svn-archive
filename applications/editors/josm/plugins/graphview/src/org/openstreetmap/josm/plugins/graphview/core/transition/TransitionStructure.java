// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.graphview.core.transition;

import java.util.Collection;

/**
 * graph-like structure for transition between OSM node/way/relation representation
 * and the WayGraph. It consists of Nodes, Segments, and Restrictions.
 */
public interface TransitionStructure {

    Collection<SegmentNode> getNodes();

    Collection<Segment> getSegments();

    Collection<Restriction> getRestrictions();

    /**
     * adds an observer.
     * Does nothing if the parameter is already an observer of this TransitionStructure.
     *
     * @param observer  observer object, != null
     */
    void addObserver(TransitionStructureObserver observer);

    /**
     * deletes an observer that has been added using {@link #addObserver(TransitionStructureObserver)}.
     * Does nothing if the parameter isn't currently an observer of this TransitionStructure.
     *
     * @param observer  observer object, != null
     */
    void deleteObserver(TransitionStructureObserver observer);

}
