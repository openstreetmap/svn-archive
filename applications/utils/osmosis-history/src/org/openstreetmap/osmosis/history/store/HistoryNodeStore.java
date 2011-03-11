// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.store;

import java.util.Collection;

import java.util.Date;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.lifecycle.Completable;


/**
 * A node location store is used for caching node locations that are
 * subsequently used to build way geometries.
 * 
 * @author Peter Koerner
 */
public interface HistoryNodeStore extends Completable {
	/**
	 * Adds the specified node.
	 * 
	 * @param node
	 *            The node
	 */
	void addNode(Node node);
	
	
	/**
	 * Get the specified node.
	 * 
	 * @param nodeId
	 *            The node identifier.
	 * @param version
	 *            The node version.
	 * @return
	 *            the node or null when not found.
	 */
	Node getNode(long nodeId, int version);
	
	
	/**
	 * Get all versions of the specified node.
	 * 
	 * @param nodeId
	 *            The node identifier.
	 * @return
	 *            the list of all versions of that node.
	 */
	Collection<Node> getNodeVersions(long nodeId);
	
	
	/**
	 * Gets the oldest node that is younger then date.
	 * 
	 * @param nodeId
	 *            The node identifier.
	 * @param date
	 *            The Date to test against.
	 * @return
	 *            the node or null when not found.
	 */
	Node findNode(long nodeId, Date date);
}
