// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Base interface for all node entities. A node entity is a lightweight wrapper
 * around OSM objects in order to ease up some tasks like tag handling.
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */
public interface IOSMEntity extends Comparable<IOSMEntity> {
    /**
     * Gets the underlying OSM object.
     * @return
     */
    public OsmPrimitive getOsmObject();

    /**
     * Checks if underlying OSM object has a name.
     * @return
     */
    public boolean hasName();

    /**
     * Gets the name of the entity node.
     * @return
     */
    public String getName();

    /**
     * Gets the children of the entity node.
     * @return
     */
    public List<IOSMEntity> getChildren();

    /**
     * Gets the coordinate of the node. If the underlying object is a
     * node, it just returns the node coordinate. For ways and areas, this
     * method returns the coordinate of the center (balance point).
     * @return
     */
    public LatLon getCoor();

    /**
     * Adds a command listener.
     * @param listener
     */
    public void addCommandListener(ICommandListener listener);

    /**
     * Removes a command listener.
     * @param listener
     */
    public void removeCommandListener(ICommandListener listener);

    /**
     * Collects problems and possible solutions.
     *
     * @param trashHeap the trash heap to ask for possible solutions
     * @param visitor the problem visitor
     */
    public void visit(IAllKnowingTrashHeap trashHeap, IProblemVisitor visitor);

    int compareTo(IOSMEntity o);
}
