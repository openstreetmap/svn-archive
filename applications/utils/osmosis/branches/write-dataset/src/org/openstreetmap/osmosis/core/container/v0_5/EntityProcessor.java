// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.container.v0_5;


/**
 * EntityContainer implementations call implementations of this class to
 * perform entity type specific processing.
 * 
 * @author Brett Henderson
 */
public interface EntityProcessor {
	
	/**
	 * Process the bound.
	 * 
	 * @param bound
	 *            The bound to be processed.
	 */
	void process(BoundContainer bound);
	
	/**
	 * Process the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	void process(NodeContainer node);

	/**
	 * Process the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	void process(WayContainer way);
	
	/**
	 * Process the relation.
	 * 
	 * @param relation
	 *            The relation to be processed.
	 */
	void process(RelationContainer relation);
}
