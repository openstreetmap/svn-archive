// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Base interface for all node entities. A node entity is a lightweight wrapper
 * around OSM objects in order to ease up some tasks like tag handling.
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public interface IOSMEntity extends Comparable<IOSMEntity> {
    /**
     * Gets the underlying OSM object.
     * @return the underlying OSM object
     */
    OsmPrimitive getOsmObject();

    /**
     * Checks if underlying OSM object has a name.
     * @return {@code true} if underlying OSM object has a name
     */
    boolean hasName();

    /**
     * Gets the name of the entity node.
     * @return the name of the entity node
     */
    String getName();

    /**
     * Gets the children of the entity node.
     * @return the children of the entity node
     */
    List<IOSMEntity> getChildren();

    /**
     * Gets the coordinate of the node. If the underlying object is a
     * node, it just returns the node coordinate. For ways and areas, this
     * method returns the coordinate of the center (balance point).
     * @return the coordinate of the node
     */
    LatLon getCoor();

    /**
     * Adds a command listener.
     * @param listener command listener
     */
    void addCommandListener(ICommandListener listener);

    /**
     * Removes a command listener.
     * @param listener command listener
     */
    void removeCommandListener(ICommandListener listener);

    /**
     * Collects problems and possible solutions.
     *
     * @param trashHeap the trash heap to ask for possible solutions
     * @param visitor the problem visitor
     */
    void visit(IAllKnowingTrashHeap trashHeap, IProblemVisitor visitor);

    @Override
    int compareTo(IOSMEntity o);
}
