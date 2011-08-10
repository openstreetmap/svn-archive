/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
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
}
